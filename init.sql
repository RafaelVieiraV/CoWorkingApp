-- init.sql
CREATE DATABASE IF NOT EXISTS coworking_db;
USE coworking_db;

CREATE TABLE IF NOT EXISTS members (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    plan_type ENUM('BASIC','STANDARD','PREMIUM'),
    monthly_hours_quota INT,
    active BIT NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS workspaces (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          name VARCHAR(100) NOT NULL,
    type ENUM('DESK','OFFICE','MEETING_ROOM','EVENT_HALL'),
    capacity INT NOT NULL,
    price_per_hour DECIMAL(8,2),
    floor INT,
    available BIT NOT NULL DEFAULT 1,
    description VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS bookings (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        member_id BIGINT NOT NULL,
                                        workspace_id BIGINT NOT NULL,
                                        start_datetime DATETIME NOT NULL,
                                        end_datetime DATETIME NOT NULL,
                                        status ENUM('PENDING','CONFIRMED','CANCELLED','COMPLETED') DEFAULT 'PENDING',
    total_hours DECIMAL(5,2),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id),
    FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
    );

-- Datos de prueba
INSERT INTO members (full_name, email, phone, plan_type, monthly_hours_quota) VALUES
                                                                                  ('Ana Torres', 'ana@espe.edu.ec', '0991111111', 'PREMIUM', 80),
                                                                                  ('Carlos Mora', 'carlos@espe.edu.ec', '0992222222', 'STANDARD', 60),
                                                                                  ('María López', 'maria@espe.edu.ec', '0993333333', 'BASIC', 40);

INSERT INTO workspaces (name, type, capacity, price_per_hour, floor, description) VALUES
                                                                                      ('Sala Innovación', 'MEETING_ROOM', 8, 20.00, 1, 'Sala equipada con proyector'),
                                                                                      ('Oficina A', 'OFFICE', 4, 15.00, 2, 'Oficina privada piso 2'),
                                                                                      ('Escritorio 01', 'DESK', 1, 5.00, 1, 'Escritorio compartido');

INSERT INTO bookings (member_id, workspace_id, start_datetime, end_datetime, status, total_hours) VALUES
                                                                                                      (1, 1, '2026-05-13 09:00:00', '2026-05-13 11:00:00', 'CONFIRMED', 2.00),
                                                                                                      (2, 2, '2026-05-13 14:00:00', '2026-05-13 16:00:00', 'CONFIRMED', 2.00);