INSERT INTO project_finance (project_id, organisation_id, organisation_size)
SELECT p.id, af.organisation_id, af.organisation_size FROM project p
JOIN application a ON a.id = p.application_id
JOIN application_finance af ON af.application_id = a.id
WHERE NOT EXISTS (SELECT 1 FROM project_finance WHERE project_id = p.id and organisation_id = af.organisation_id);