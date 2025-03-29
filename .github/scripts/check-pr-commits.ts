// check-pr-commits.ts

import { delay } from "https://deno.land/std@0.95.0/async/delay.ts";

const GITHUB_TOKEN = Deno.env.get("GITHUB_TOKEN");
const REPO_OWNER = Deno.env.get("GITHUB_REPOSITORY_OWNER");
const REPO_NAME = Deno.env.get("GITHUB_REPOSITORY_NAME");
const PR_NUMBER = Deno.env.get("GITHUB_PR_NUMBER");
const CHECK_NAME = "Branch build";
const TIMEOUT = 600000;
const POLL_INTERVAL = 10000;

if (!GITHUB_TOKEN || !REPO_OWNER || !REPO_NAME || !PR_NUMBER) {
  console.error("Missing required environment variables.");
  Deno.exit(1);
}

const headers = {
  "Authorization": `Bearer ${GITHUB_TOKEN}`,
  "Accept": "application/vnd.github.v3+json",
};

try {
  const commitShas = await getPullRequestCommits();
  await Promise.all(commitShas.map(commitSha => waitForWorkflowSuccess(commitSha)));
  console.log("All commits have passed successfully.");
} catch (error: unknown) {
  console.error(`Error: ${error}`);
  Deno.exit(1);
}

async function fetchJson(url: string) {
  const response = await fetch(url, { headers });
  if (!response.ok) {
    throw new Error(`GitHub API request failed: ${response.statusText}`);
  }
  return response.json();
}

async function getPullRequestCommits(): Promise<string[]> {
  const url =
    `https://api.github.com/repos/${REPO_OWNER}/${REPO_NAME}/pulls/${PR_NUMBER}/commits`;
  const commits = await fetchJson(url);
  return commits.map((commit: { sha: string }) => commit.sha);
}

async function getWorkflowStatus(commitSha: string): Promise<string> {
  const url =
    `https://api.github.com/repos/${REPO_OWNER}/${REPO_NAME}/commits/${commitSha}/status`;
  const status = await fetchJson(url);
  const check = status.statuses.find((s: { context: string }) =>
    s.context === CHECK_NAME
  );
  return check ? check.state : "pending";
}

async function waitForWorkflowSuccess(commitSha: string) {
  const startTime = Date.now();
  while (Date.now() - startTime < TIMEOUT) {
    const status = await getWorkflowStatus(commitSha);
    if (status === "success") {
      console.log(`Commit ${commitSha} passed.`);
      return;
    } else if (status === "failure") {
      console.error(`Commit ${commitSha} failed.`);
      Deno.exit(1);
    }
    console.log(
      `Commit ${commitSha} is in progress. Checking again in ${POLL_INTERVAL / 1000
      } seconds...`,
    );
    await delay(POLL_INTERVAL);
  }
  console.error(`Timeout waiting for commit ${commitSha} to pass.`);
  Deno.exit(1);
}
