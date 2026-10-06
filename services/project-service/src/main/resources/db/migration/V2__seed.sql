-- Sample data, loaded once on first start (see docs/seed-data.md). Never edit after release.
INSERT INTO users (id, name, role) VALUES
  ('00000000-0000-0000-0000-000000000001', 'Priya Shah',    'PRODUCT_MANAGER'),
  ('00000000-0000-0000-0000-000000000002', 'Marco Rossi',   'ENGINEER'),
  ('00000000-0000-0000-0000-000000000003', 'Lena Fischer',  'ENGINEER'),
  ('00000000-0000-0000-0000-000000000004', 'Tom Nguyen',    'ENGINEER'),
  ('00000000-0000-0000-0000-000000000005', 'Sara Okafor',   'ENGINEER');

INSERT INTO projects (id, name, description, created_by, created_at) VALUES
  ('10000000-0000-0000-0000-000000000001', 'Website Redesign',
   'Refresh the company website: content, design and accessibility.',
   '00000000-0000-0000-0000-000000000001', TIMESTAMP WITH TIME ZONE '2026-10-01 09:00:00+00'),
  ('10000000-0000-0000-0000-000000000002', 'Mobile App Launch',
   'Prepare the first public release of the mobile app.',
   '00000000-0000-0000-0000-000000000001', TIMESTAMP WITH TIME ZONE '2026-10-01 09:00:01+00'),
  ('10000000-0000-0000-0000-000000000003', 'Internal Tooling',
   'Small tools and automation for the team.',
   '00000000-0000-0000-0000-000000000001', TIMESTAMP WITH TIME ZONE '2026-10-01 09:00:02+00');
