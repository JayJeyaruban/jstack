import { delay } from "https://deno.land/std@0.95.0/async/delay.ts";

const GITHUB_URL = 'https://api.github.com'
const GITHUB_TOKEN = Deno.env.get("GITHUB_TOKEN");
const REPO_OWNER = Deno.env.get("GITHUB_REPOSITORY_OWNER");
const REPO_NAME = Deno.env.get("GITHUB_REPOSITORY_NAME");
const PR_NUMBER = Deno.env.get("GITHUB_PR_NUMBER");
const CHECK_NAME = "Branch build";
const TIMEOUT = 600000;
const POLL_INTERVAL = 10000;

type WorkflowRun = {
  head_sha: string,
  conclusion?: 'success' | 'failure'
  updated_at: string,
};

if (!GITHUB_TOKEN || !REPO_OWNER || !REPO_NAME || !PR_NUMBER) {
  console.error("Missing required environment variables.");
  Deno.exit(1);
}

const headers = {
  "Authorization": `Bearer ${GITHUB_TOKEN}`,
  "Accept": "application/vnd.github.v3+json",
};

try {
  const [workflowId, commitShas] = await Promise.all([getWorkflowId(), getPullRequestCommits()]);
  console.log('Commits:', commitShas);
  let runs: WorkflowRun[] = await getWorkflowRuns(workflowId);
  runs = keepMostRecentSha(runs);
  const successfulRuns = new Set(runs.filter(run => run.conclusion === 'success').map(run => run.head_sha));
  const failedRuns = new Set(runs.filter(run => run.conclusion === 'failure').map(runs => runs.head_sha));
  let failedShas = commitShas.filter(sha => failedRuns.has(sha));
  console.debug('Failed shas:', failedShas);
  const pendingShas = commitShas.filter(sha => !failedRuns.has(sha) && !successfulRuns.has(sha))
  console.log(`Pending shas: ${pendingShas}`);
  failedShas = failedShas.concat(await Promise.all(pendingShas.filter(sha => !waitForWorkflowSuccess(workflowId, sha))));
  console.debug('Final failed shas:', failedShas);

  if (failedShas.length === 0) {
    console.log("All commits have passed successfully.");
  } else {
    console.error(`Commits failed the check: ${failedShas}`);
    Deno.exit(1);
  }
} catch (error: unknown) {
  console.error(`Error: ${error}`);
  Deno.exit(1);
}

async function getWorkflowId(): Promise<number> {
  const url = `${GITHUB_URL}/repos/${REPO_OWNER}/${REPO_NAME}/actions/workflows`
  const result = await fetchJson(url);
  for (const workflow of result.workflows) {
    if (workflow.name === CHECK_NAME) {
      return workflow.id;
    }
  }

  throw Error(`Unable to identify ID for ${CHECK_NAME}`);
}

async function getWorkflowRuns(workflowId: number) {
  const url = `${GITHUB_URL}/repos/${REPO_OWNER}/${REPO_NAME}/actions/workflows/${workflowId}/runs?branch=build&per_page=20`
  const result = await fetchJson(url);
  return result.workflow_runs;
}

async function getPullRequestCommits(): Promise<string[]> {
  const url =
    `${GITHUB_URL}/repos/${REPO_OWNER}/${REPO_NAME}/pulls/${PR_NUMBER}/commits`;
  const commits = await fetchJson(url);
  return commits.map((commit: { sha: string }) => commit.sha);
}

async function getWorkflowStatus(workflowId: number, commitSha: string): Promise<WorkflowRun['conclusion']> {
  const url = `${GITHUB_URL}/repos/${REPO_OWNER}/${REPO_NAME}/actions/workflows/${workflowId}/runs?branch=build&per_page=20&head_sha=${commitSha}`
  const res = await fetchJson(url);
  let runs: WorkflowRun[] = res.workflow_runs;
  if (runs.length === 0) {
    return undefined;
  }
  runs = keepMostRecentSha(runs);
  return runs[0].conclusion;
}

async function waitForWorkflowSuccess(workflowId: number, commitSha: string): Promise<boolean> {
  const startTime = Date.now();
  while (Date.now() - startTime < TIMEOUT) {
    const status = await getWorkflowStatus(workflowId, commitSha);
    if (status === "success") {
      console.log(`Commit ${commitSha} passed.`);
      return true;
    } else if (status === "failure") {
      console.error(`Commit ${commitSha} failed.`);
      return false;
    }
    console.log(
      `Commit ${commitSha} is in progress. Checking again in ${POLL_INTERVAL / 1000
      } seconds...`,
    );
    await delay(POLL_INTERVAL);
  }
  console.error(`Timeout waiting for commit ${commitSha} to pass.`);
  return false;
}

function keepMostRecentSha(runs: WorkflowRun[]): WorkflowRun[] {
  runs = runs.sort(byDate);
  const res = [];
  const seen = new Set();
  for (const run of runs) {
    if (seen.has(run.head_sha)) {
      continue;
    }

    res.push(run);
  }

  return res;
}

function byDate(r1: WorkflowRun, r2: WorkflowRun): number {
  return Date.parse(r2.updated_at) - Date.parse(r1.updated_at)
}

async function fetchJson(url: string) {
  const response = await fetch(url, { headers });
  if (!response.ok) {
    throw new Error(`GitHub API request failed: ${response.statusText}`);
  }
  return response.json();
}
