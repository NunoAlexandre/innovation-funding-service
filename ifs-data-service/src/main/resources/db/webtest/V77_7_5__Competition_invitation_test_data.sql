INSERT INTO invite (id, email, hash, name, status, target_id, owner_id, type) VALUES (18, 'worth.email.test+assessor1@gmail.com', '469ffd4952ce0a4c310ec09a1175fb5abea5bc530c2af487f32484e17a4a3776c2ec430f3d957471', 'Assessor One', 'SENT', 4, 18, 'COMPETITION');
INSERT INTO competition_user (competition_role_id, competition_id, user_id, invite_id, rejection_reason_id, rejection_comment, participant_status_id) VALUES (1, 4, 53, 18, null, null, 1);