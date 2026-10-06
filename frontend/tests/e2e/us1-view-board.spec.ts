import { expect, test } from '@playwright/test';

/** Quickstart scenario 1: choose a user, open each sample project, see four columns with seeded tasks. */
test('choose a user and view the board', async ({ page }) => {
  await page.goto('/');

  // exactly five users: one product manager, four engineers
  const users = page.getByRole('list', { name: 'Users' }).getByRole('listitem');
  await expect(users).toHaveCount(5);
  await expect(page.getByText('Product manager')).toHaveCount(1);
  await expect(page.getByText('Engineer', { exact: true })).toHaveCount(4);

  await page.getByText('Marco Rossi').click();

  // three sample projects
  const projects = page.getByRole('list', { name: 'Projects' }).getByRole('listitem');
  await expect(projects).toHaveCount(3);

  for (const name of ['Website Redesign', 'Mobile App Launch', 'Internal Tooling']) {
    await page.getByRole('list', { name: 'Projects' }).getByText(name).click();
    const headings = page.getByRole('heading', { level: 2 });
    await expect(headings).toHaveText([/To Do \(2\)/, /In Progress \(2\)/, /In Review \(2\)/, /Done \(2\)/]);
    await expect(page.getByTestId('task-card')).toHaveCount(8);
    await page.getByRole('link', { name: 'All projects' }).click();
  }

  // the selection survives a reload
  await page.reload();
  await expect(page.getByText('Acting as Marco Rossi')).toBeVisible();
});
