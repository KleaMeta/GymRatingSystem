CREATE DATABASE IF NOT EXISTS gym_rating_system;

USE gym_rating_system;

-- Create Users Table
CREATE TABLE IF NOT EXISTS Users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,
    is_admin BOOLEAN DEFAULT FALSE
);

-- Create Gyms Table
CREATE TABLE IF NOT EXISTS Gyms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    zip_code VARCHAR(10) NOT NULL,
    type ENUM('Gym', 'Yoga Studio', 'Pilates Studio') NOT NULL,
    hours VARCHAR(100),
    equipment TEXT
);

-- Create RatingsAndReviews Table
CREATE TABLE IF NOT EXISTS RatingsAndReviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    gym_id INT NOT NULL,
    user_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    review TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (gym_id) REFERENCES Gyms(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
);

-- Insert users
INSERT IGNORE INTO Users (email, password, zip_code, is_admin)
VALUES 
('admin@example.com', 'hashed_password_123', '12345', TRUE),
('user1@example.com', 'hashed_password_456', '67890', FALSE);

-- Insert gyms
INSERT INTO Gyms (name, address, zip_code, type, hours, equipment)
VALUES
-- Gyms
('Equinox Wall Street', '14 Wall St, New York, NY', '10005', 'Gym', '5:30 AM - 10:00 PM', 'Cardio Machines, Free Weights'),
('Blink Fitness NoHo', '4 Astor Pl, New York, NY', '10003', 'Gym', '5:00 AM - 11:00 PM', 'Cardio Machines, Strength Training'),
('New York Sports Clubs FiDi', '30 Broad St, New York, NY', '10004', 'Gym', '6:00 AM - 10:00 PM', 'Full Gym, Group Classes'),
('Planet Fitness LES', '22 E 14th St, New York, NY', '10003', 'Gym', '24 Hours', 'Cardio Machines, Strength Equipment'),
('Crunch Fitness Bowery', '2 Cooper Sq, New York, NY', '10003', 'Gym', '5:00 AM - 11:00 PM', 'Cardio, Free Weights, Classes'),
('YMCA Chinatown', '273 Bowery, New York, NY', '10002', 'Gym', '6:00 AM - 10:00 PM', 'Swimming Pool, Full Gym'),
('SoulCycle NoHo', '632 Broadway, New York, NY', '10012', 'Gym', 'Varies by Class Schedule', 'Indoor Cycling Studio'),
('Barrys Bootcamp Tribeca', '1 York St, New York, NY', '10013', 'Gym', 'Varies by Class Schedule', 'Treadmills, HIIT Equipment'),
('Rumble Boxing NoHo', '700 Broadway, New York, NY', '10003', 'Gym', 'Varies by Class Schedule', 'Boxing-Inspired Equipment'),
('Orangetheory Fitness FiDi', '30 Broad St, New York, NY', '10004', 'Gym', 'Varies by Class Schedule', 'Rowing Machines, Weights'),

-- Yoga Studios
('Y7 Studio SoHo', '430 Broome St, New York, NY', '10013', 'Yoga Studio', 'Varies by Class Schedule', 'Heated Yoga, Music'),
('Sky Ting Yoga Chinatown', '17 Allen St, New York, NY', '10002', 'Yoga Studio', 'Varies by Class Schedule', 'Vinyasa Flow, Props'),
('Kula Yoga Project Tribeca', '28 Warren St, New York, NY', '10007', 'Yoga Studio', 'Varies by Class Schedule', 'Alignment-Based Classes'),
('Modo Yoga NYC', '434 6th Ave, New York, NY', '10011', 'Yoga Studio', 'Varies by Class Schedule', 'Hot Yoga Studio'),
('Yoga Vida NoHo', '99 University Pl, New York, NY', '10003', 'Yoga Studio', 'Varies by Class Schedule', 'Accessible Classes'),
('Pure Yoga East', '203 E 86th St, New York, NY', '10028', 'Yoga Studio', 'Varies by Class Schedule', 'Workshops, Styles'),
('Laughing Lotus Yoga Center', '636 Sixth Ave, New York, NY', '10011', 'Yoga Studio', 'Varies by Class Schedule', 'Dynamic Flow'),
('Integral Yoga Institute', '227 W 13th St, New York, NY', '10011', 'Yoga Studio', 'Varies by Class Schedule', 'Hatha Yoga, Teacher Training'),
('Zen Yoga Studio', '450 E 86th St, New York, NY', '10028', 'Yoga Studio', '6:00 AM - 9:00 PM', 'Mats, Blocks'),

-- Pilates Studios
('Dynamic Body Pilates', '80 5th Ave, New York, NY 10003', '10003', 'Pilates Studio', 'By Appointment', 'Mobility, Rehabilitation'),
('Core Pilates NYC', '900 Broadway, New York, NY 10003', '10003', 'Pilates Studio', 'Varies by Class Schedule', 'Mat, Reformer'),
('New York Pilates SoHo', '25 Howard St, New York, NY 10012', '10012', 'Pilates Studio', 'Varies by Class Schedule', 'Reformer Classes'),
('BodyRok SoHo', '598 Broadway, New York, NY 10012', '10012', 'Pilates Studio', 'Varies by Class Schedule', 'Hybrid Pilates Movements on Custom Reformer'),
('Mongoose Bodyworks', '594 Broadway, New York, NY 10013', '10013', 'Pilates Studio', 'Varies by Class Schedule', 'Core Strength, Personalized Sessions'),
('Natural Pilates - SoHo', '206 Spring St, New York, NY 10013', '10013', 'Pilates Studio', 'Varies by Class Schedule', 'Challenging, Effective Workouts'),
('Sixth Street Pilates', '525 E 6th St, New York, NY 10002', '10002', 'Pilates Studio', 'Varies by Class Schedule', 'Mat, Springboard, Reformer'),
('Avea Pilates', '130 E 7th St, New York, NY 10002', '10002', 'Pilates Studio', 'Varies by Class Schedule', 'Therapeutic Exercises for Pain Management'),
('Pilates People NYC', '145 Broadway, New York, NY 10005', '10005', 'Pilates Studio', 'Varies by Class Schedule', 'Contemporary Reformer Classes'),
('New York Pilates FiDi', '30 Broad St, New York, NY 10004', '10005', 'Pilates Studio', 'Varies by Class Schedule', 'Core Strength, Flexibility');

-- Insert ratings and reviews
INSERT IGNORE INTO RatingsAndReviews (gym_id, user_id, rating, review)
VALUES 
(1, 2, 5, 'Great gym with excellent equipment!'),
(2, 2, 4, 'Nice yoga studio but limited hours.');

