INSERT INTO roles (name) VALUES ('ADMIN'), ('MANAGER'), ('USER');

INSERT INTO permissions (code, description) VALUES
('USER_READ',       'Чтение пользователей'),
('USER_WRITE',      'Создание и редактирование пользователей'),
('ROLE_READ',       'Чтение ролей'),
('ROLE_WRITE',      'Управление ролями'),
('PERMISSION_READ', 'Чтение разрешений');

INSERT INTO users (email, name, active) VALUES
('admin@example.com',   'Администратор',       TRUE),
('manager@example.com', 'Менеджер',            TRUE),
('user@example.com',    'Обычный пользователь', TRUE),
('blocked@example.com', 'Заблокированный',      FALSE);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'admin@example.com'   AND r.name = 'ADMIN';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'manager@example.com' AND r.name = 'MANAGER';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'user@example.com'    AND r.name = 'USER';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'blocked@example.com' AND r.name = 'USER';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ADMIN';
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'MANAGER' AND p.code IN ('USER_READ', 'USER_WRITE', 'ROLE_READ');
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'USER' AND p.code = 'USER_READ';
