-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost:8889
-- Generation Time: Sep 08, 2025 at 12:58 PM
-- Server version: 8.0.40
-- PHP Version: 8.3.14

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `ex4`
--
CREATE DATABASE IF NOT EXISTS `ex4` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `ex4`;

-- --------------------------------------------------------

--
-- Table structure for table `events`
--

CREATE TABLE `events` (
  `event_id` bigint NOT NULL,
  `date_of_creation` datetime(6) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `event_name` varchar(100) NOT NULL,
  `status` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `events`
--

INSERT INTO `events` (`event_id`, `date_of_creation`, `description`, `event_name`, `status`) VALUES
(34, '2025-08-17 19:03:22.374420', NULL, 'Ninja Israel 2026', 'not-active'),
(35, '2025-08-17 19:05:33.536979', 'זמר : עדן חסון, תאריך : 14/05/2026', 'הופעת זמר פארק הירקון - 14/05', 'not-active'),
(36, '2025-08-17 19:08:53.441551', 'a large scale cyber security event, more than 2000 people.', 'Cyber Competition Event', 'active'),
(37, '2025-08-17 19:11:28.181150', 'graduation ceremony for Hadassah\'s Academic Collage in center of jerusalem. location is yet unknown.', 'HAC\'s Graduation Ceremony', 'not-active'),
(38, '2025-08-17 19:14:40.444266', 'אירוע גדול מתרחש בחוף הגולשים למשך שלושה ימים ומשתתפים בו כאלף איש.', 'Israeli Surfing Association Meetup', 'active');

-- --------------------------------------------------------

--
-- Table structure for table `event_responsibilities`
--

CREATE TABLE `event_responsibilities` (
  `id` bigint NOT NULL,
  `event_id` bigint NOT NULL,
  `responsibility_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `event_responsibilities`
--

INSERT INTO `event_responsibilities` (`id`, `event_id`, `responsibility_id`) VALUES
(80, 38, 25),
(83, 38, 24),
(84, 38, 28),
(85, 36, 24),
(86, 36, 25),
(87, 36, 26),
(88, 36, 27),
(89, 36, 28);

-- --------------------------------------------------------

--
-- Table structure for table `items`
--

CREATE TABLE `items` (
  `item_id` bigint NOT NULL,
  `item_name` varchar(32) NOT NULL,
  `status` varchar(20) NOT NULL,
  `responsibility_id` bigint NOT NULL,
  `user_id` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `items`
--

INSERT INTO `items` (`item_id`, `item_name`, `status`, `responsibility_id`, `user_id`) VALUES
(61, 'thinkpad 15 - 252', 'Available', 24, NULL),
(62, 'thinkpad 15 - 542', 'Available', 24, NULL),
(63, 'thinkpad 16 - 122', 'Available', 24, NULL),
(64, 'thinkpad 14 - 423', 'Available', 24, NULL),
(65, 'macbook air 13 - 103', 'Available', 24, NULL),
(66, 'macbook pro 14 - 232', 'Unavailable', 24, NULL),
(67, 'macbook pro 16 - 110', 'Unavailable', 24, NULL),
(68, 'Star Caravan - 1 Person - 72342', 'Available', 28, NULL),
(69, 'Star Caravan - 1 Person - 72344', 'Available', 28, NULL),
(70, 'Star Caravan - 1 Person - 72346', 'Available', 28, NULL),
(71, 'Star Caravan - 2 Person - 72142', 'Available', 28, NULL),
(72, 'Star Caravan - 2 Person - 72123', 'Unavailable', 28, NULL),
(73, 'Toilet Caravan - 01', 'Available', 28, NULL),
(74, 'Toilet Caravan - 02', 'Available', 28, NULL),
(75, 'Work Caravan - 01', 'Available', 28, NULL),
(76, 'Work Caravan - 02', 'Available', 28, NULL),
(77, 'Work Caravan - 03', 'Available', 28, NULL),
(78, '10x10 ft - platform 1', 'Available', 27, NULL),
(79, '10x10 ft - platform 2', 'Available', 27, NULL),
(80, '12x12 ft - platform 3', 'Available', 27, NULL),
(81, '24x24 ft - wooden platform', 'Available', 27, NULL),
(82, 'LED dance floor - 1', 'Available', 27, NULL),
(83, 'LED dance floor - 2', 'Available', 27, NULL),
(84, 'VIP risers', 'Unavailable', 27, NULL),
(85, 'Microphones - set 523', 'Available', 26, NULL),
(86, 'Microphones - set 524', 'Available', 26, NULL),
(87, 'Microphones - set 525', 'Available', 26, NULL),
(88, 'Microphones - set 526', 'Unavailable', 26, NULL),
(89, 'Amplifiers - set 1', 'Available', 26, NULL),
(90, 'Amplifiers - set 2', 'Available', 26, NULL),
(91, 'Amplifiers - set 3', 'Available', 26, NULL),
(92, 'Amplifiers - set 4', 'Unavailable', 26, NULL),
(93, 'Playback set - 12', 'Available', 26, NULL),
(94, 'Playback set - 14', 'Available', 26, NULL),
(95, 'DI box set - 2', 'Available', 26, NULL),
(96, 'DI box set - 3', 'Unavailable', 26, NULL),
(97, 'Big Stage Light set 1', 'Available', 26, NULL),
(98, 'Big Stage Light set 2', 'Available', 26, NULL),
(99, 'Big Stage Light set 3', 'Available', 26, NULL),
(100, 'DMX Controller - 341', 'Available', 26, NULL),
(101, 'DMX Controller - 342', 'Available', 26, NULL),
(102, 'DMX Controller - 340', 'Available', 26, NULL),
(103, 'Toyota - 2345233', 'Available', 25, NULL),
(104, 'Toyota - 4512322', 'Unavailable', 25, NULL),
(105, 'Toyota - 2345231', 'Available', 25, NULL),
(106, 'Toyota - 70423401', 'Available', 25, NULL),
(107, 'Hyundai - 72140401', 'Available', 25, NULL),
(108, 'Hyundai - 72114244', 'Available', 25, NULL),
(109, 'Hyundai - 12345401', 'Available', 25, NULL),
(110, 'Honda - 6500257', 'Available', 25, NULL),
(111, 'Ford - 23345100', 'Available', 25, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `requests`
--

CREATE TABLE `requests` (
  `request_id` bigint NOT NULL,
  `date_of_issue` datetime(6) NOT NULL,
  `request_type` varchar(10) NOT NULL,
  `item_id` bigint NOT NULL,
  `user_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `responsibilities`
--

CREATE TABLE `responsibilities` (
  `responsibility_id` bigint NOT NULL,
  `responsibility_name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `responsibilities`
--

INSERT INTO `responsibilities` (`responsibility_id`, `responsibility_name`, `description`) VALUES
(24, 'Computers', 'המחשבים מגיעים עם כל הציוד הנלווה, המחשב, תיק מחשב ועכבר.'),
(25, 'Vehicles', 'All vehicles work! Take care of them as you received them and return them.'),
(26, 'Lights and Amplification', NULL),
(27, 'Platforms', NULL),
(28, 'Caravans', 'Trailers must be connected to vehicles that have a trailer! You will arrive to pick up with a vehicle that has a trailer (usually for Toyotas)');

-- --------------------------------------------------------

--
-- Table structure for table `roles`
--

CREATE TABLE `roles` (
  `role_id` bigint NOT NULL,
  `name` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `roles`
--

INSERT INTO `roles` (`role_id`, `name`) VALUES
(1, 'admin'),
(2, 'chief'),
(3, 'manager'),
(4, 'user');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` bigint NOT NULL,
  `date_of_issue` datetime(6) NOT NULL,
  `email_address` varchar(255) NOT NULL,
  `encrypted_password` varchar(255) NOT NULL,
  `phone_number` varchar(255) NOT NULL,
  `first_name` varchar(20) NOT NULL,
  `last_name` varchar(20) NOT NULL,
  `role_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `date_of_issue`, `email_address`, `encrypted_password`, `phone_number`, `first_name`, `last_name`, `role_id`) VALUES
(1, '2025-06-01 09:25:28.000000', 'admin@admin.com', '$2a$10$yRWnac1o4NJqMdRKm1zXneaS9l1APA2l2ZKelPitACPTcjy8rY9wm', '0000000000', 'admin', 'admin', 1),
(217, '2025-08-17 18:52:09.919473', 'chief1@gmail.com', '$2a$10$loFpSXeRIedgrTuV./9RK.pJma4iavWfvqAy93IPdTIEy7vz/r4NO', '0543233322', 'eithan', 'solomon', 2),
(218, '2025-08-17 18:55:57.147710', 'chief2@gmail.com', '$2a$10$VropbjgSMEH3dvfFLfDsMeiFsjmPzV0vV/QhtPOsmou2e99QdNcx.', '0502340900', 'shely', 'burman', 2),
(219, '2025-08-17 18:56:28.289704', 'chief3@gmail.com', '$2a$10$7ymfT9yuWO7X6xkcpwt2gO.MUO85CpZiiZrnp/B32cBwBR7bXmxVa', '0504432133', 'david', 'ruthman', 2),
(220, '2025-08-17 18:59:31.237635', 'user1@gmail.com', '$2a$10$9mQfPcGhvsgWYog40e4VsOqsmiVcND.uZZgGx0rzYCeti4OxIUo5.', '0542323245', 'alex', 'naveh', 3),
(221, '2025-08-17 19:21:41.816318', 'user2@gmail.com', '$2a$10$sZCN4IF1dTXMMX/.eBBDwONhqQAgzm46O89fwvWDHPw8da.cYE7Qy', '0522876623', 'david', 'kol', 3),
(222, '2025-08-17 19:22:48.235188', 'user3@gmail.com', '$2a$10$j0y9tkYAGuIwm8ARgDY7leHsniyXDQTnvLAqEYwioRB/Y8GYmGNhG', '0502322122', 'tomas', 'fillard', 3),
(223, '2025-08-17 19:23:28.800038', 'user4@gmail.com', '$2a$10$SLii6fTS7LtYgJuonGgq/OWWeW/0tGM2/MnYo/YmMbiwIANvJ1pMS', '0503239922', 'gabriel', 'samaburg', 3),
(224, '2025-08-17 19:24:27.129106', 'user5@gmail.com', '$2a$10$dnsvfoXocNpGHeCU1rfmWegmpIdIn3uN3SvgM9T4i9.MWOqQRU87O', '0541020333', 'simon', 'sing', 3),
(225, '2025-08-17 19:25:11.790793', 'user6@gmail.com', '$2a$10$2RtIdHzTE010/uvPDznmBeodHNNe2gpzxAmKJyCTn4fLSAckU1kP6', '0522344402', 'ron', 'leshem', 3),
(226, '2025-08-17 19:26:06.055507', 'user7@gmail.com', '$2a$10$G6.amEIAMUw2FkcoT/pG3ukJu.31m1LFgvwt/85Z9jaKeOJDdQECS', '0532233233', 'matan', 'cohen', 3),
(227, '2025-08-17 19:26:42.336897', 'user8@gmail.com', '$2a$10$gnicakC/NmbFrbt8iPWa7.pyRzBy7OLxWiO/yeWUcHGLJfAKj5vA2', '0523242411', 'frank', 'simon', 3),
(228, '2025-08-17 19:27:11.935837', 'user9@gmail.com', '$2a$10$NEGipO5bswShKR61c5HwXeYBqfMYz6gaYccx3gfhfpLZ8a8V6PM7K', '0524243244', 'john', 'gershon', 3),
(229, '2025-08-17 19:27:52.617694', 'user10@gmail.com', '$2a$10$KkJOzUUE5jr/HjCnRzZVauTJU0D7n/scrbs/7xiLKK1RsfMmzYaKa', '0502323333', 'ruth', 'beere', 3),
(230, '2025-08-17 19:58:42.028463', 'user11@gmail.com', '$2a$10$IQVDbs9vzP8sMkGm7EWQ9u0f9Q6gdNxSCkxjoEQvPl3CxRIXYaCrq', '0504998233', 'dimirti', 'dragonov', 4),
(231, '2025-08-17 19:59:09.030029', 'user12@gmail.com', '$2a$10$AXtuD3MQu45UdqWBhdto/.PRFdDwhzf/ASB1fwrgkCS5veWsgslfO', '0542324424', 'twelve', 'sunday', 4),
(232, '2025-08-17 19:59:34.554252', 'user13@gmail.com', '$2a$10$ZS6eyLAjGW0val4DeOX2l.1AD5pfSsCXPsmWU4UR57.4qJnE88p0O', '0508732937', 'rami', 'levy', 4),
(233, '2025-08-17 19:59:57.563971', 'user14@gmail.com', '$2a$10$rA/KfxnKYh8kHrz57hIw3OD9V0RCdTQm1rYr8onGHdyWJc73v901i', '0562443233', 'ein', 'gedy', 4),
(234, '2025-08-17 20:00:51.110600', 'user15@gmail.com', '$2a$10$.elpVlpkqBpYvfvt93NztOxbsUmoGoBn07wROMt/8an68AlsZdjhC', '0542599283', 'koby', 'british', 4),
(235, '2025-08-17 20:01:25.307313', 'user16@gmail.com', '$2a$10$PMLcZXt0oyKL9l5pTlX9VepCfCAIXtxdT.DyU/mMcQJI5oBTE5y9i', '0588232155', 'rony', 'sefer', 4),
(236, '2025-08-17 20:01:59.695880', 'user17@gmail.com', '$2a$10$X7jhMlJuAldh5TY.HeCyKuIluJOD3c4bOYY/dupA.3cs7s/kQx4jm', '0523242515', 'rahel', 'salvador', 4),
(237, '2025-08-17 20:02:18.312185', 'user18@gmail.com', '$2a$10$awTZvM8BK7BB/A5x43BGweurhgw8EadlXYWEzfKoLRqVDp3esqG5e', '0522298739', 'simha', 'rozen', 4),
(238, '2025-08-17 20:02:57.610921', 'user19@gmail.com', '$2a$10$Haxaawq7L9TIQWQ53KcN2OyMYp74UGzeIxAVWdMDXKyYs3UAb9QFe', '0533985757', 'steve', 'jobs', 4),
(239, '2025-08-17 20:03:36.551780', 'user20@gmail.com', '$2a$10$vr0JoyZO7lVqYWMlwh2hiukZbozJ3prq4v7FDbdXDE8CNgEQXxcYm', '0523345042', 'silvy', 'tornado', 4),
(240, '2025-09-08 12:06:42.479699', 'user21@gmail.com', '$2a$10$BmLgVds6jXwRhsEDJxH8R.HUJRcVTdu/2X/dOcfBXiOhGS8soHz/S', '0542987233', 'tamar', 'cohen', 4),
(241, '2025-09-08 12:08:37.813415', 'user22@gmail.com', '$2a$10$DaL3z4Y3jpytxAmdpcas0.Nswt9Slb.JmpHP0RK33dwxhLgF.76.6', '0525923200', 'roni', 'salamon', 4),
(242, '2025-09-08 12:09:27.422861', 'user23@gmail.com', '$2a$10$Goe.8IJ6vFU1FtW3wbFufuARz/.NPglOqZ.B7tBOFq74JfExJ5cdi', '0508992442', 'tamir', 'turgeman', 4),
(243, '2025-09-08 12:10:05.256571', 'user24@gmail.com', '$2a$10$1tASqEj/qTM2DlsZztZV5.ZdjcvqtSyH6SO7WDPSRB0ULXjyWRXoy', '0550990111', 'max', 'berman', 4),
(244, '2025-09-08 12:10:54.705803', 'user25@gmail.com', '$2a$10$7PyXT5ibwNClvw3IaUqDiuWlPK6IfyQuyuv0Jusd2XqvcRa1X7qRi', '0559823030', 'kelly', 'slater', 4),
(245, '2025-09-08 12:11:52.999694', 'user26@gmail.com', '$2a$10$dikwm7v822HD4DFOwB9ZBeo2kn.w5ab.ZrG/GTBmysQtyv3/V4vpm', '0523049582', 'gabriel', 'medina', 4),
(246, '2025-09-08 12:12:54.071828', 'user27@gmail.com', '$2a$10$ci.C0zl7RtuMjPBjSvltV.0x3pT6XeX231OgMRtG6xNYrk9m0sP3G', '0502900923', 'duke', 'kahanamoku', 4),
(247, '2025-09-08 12:13:50.194306', 'user28@gmail.com', '$2a$10$ZdLvAMuIOhCWYjztkLWwZOFltQZ09k3IztoqQ9vYgF9jxk.42PDlW', '0521129882', 'mick', 'fanning', 4),
(248, '2025-09-08 12:14:25.740880', 'user29@gmail.com', '$2a$10$peUq9W67ywqfvCebGLX8tuZ1InPD09N6.CQGOVMdWYY.UnFh2fzBa', '0501022985', 'carissa', 'moore', 4),
(249, '2025-09-08 12:15:25.067811', 'user30@gmail.com', '$2a$10$Y26DAR1NjIZJq7bWgBX5XuERFERP.sLz6ZOmYes82h/l4yHMQaHHe', '0527894311', 'laird', 'hamilton', 4),
(250, '2025-09-08 13:40:15.378162', 'user31@gmail.com', '$2a$10$v.H4YU4KvAB7vpoeotuN3OaEgdWynL9x73vUlJKt7BQO1/GCnDX7G', '0502324151', 'terry', 'longman', 4);

-- --------------------------------------------------------

--
-- Table structure for table `user_responsibilities`
--

CREATE TABLE `user_responsibilities` (
  `id` bigint NOT NULL,
  `responsibility_id` bigint NOT NULL,
  `user_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `user_responsibilities`
--

INSERT INTO `user_responsibilities` (`id`, `responsibility_id`, `user_id`) VALUES
(49, 24, 220),
(50, 25, 225),
(51, 26, 221),
(52, 27, 224),
(53, 28, 228),
(54, 24, 229),
(55, 25, 223),
(56, 25, 222),
(57, 26, 226),
(58, 25, 227);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `events`
--
ALTER TABLE `events`
  ADD PRIMARY KEY (`event_id`);

--
-- Indexes for table `event_responsibilities`
--
ALTER TABLE `event_responsibilities`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK_event_responsibility_event` (`event_id`),
  ADD KEY `FK_event_responsibility_responsibility` (`responsibility_id`);

--
-- Indexes for table `items`
--
ALTER TABLE `items`
  ADD PRIMARY KEY (`item_id`),
  ADD KEY `FK_item_responsibility` (`responsibility_id`),
  ADD KEY `FK_item_user` (`user_id`);

--
-- Indexes for table `requests`
--
ALTER TABLE `requests`
  ADD PRIMARY KEY (`request_id`),
  ADD KEY `FK_request_item` (`item_id`),
  ADD KEY `FK_request_user` (`user_id`);

--
-- Indexes for table `responsibilities`
--
ALTER TABLE `responsibilities`
  ADD PRIMARY KEY (`responsibility_id`);

--
-- Indexes for table `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`role_id`),
  ADD UNIQUE KEY `UKofx66keruapi6vyqpv6f2or37` (`name`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `UK1ar956vx8jufbghpyi09yr16l` (`email_address`),
  ADD UNIQUE KEY `UK9q63snka3mdh91as4io72espi` (`phone_number`),
  ADD KEY `FK_user_role` (`role_id`);

--
-- Indexes for table `user_responsibilities`
--
ALTER TABLE `user_responsibilities`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK_user_responsibility_responsibility` (`responsibility_id`),
  ADD KEY `FK_user_responsibility_user` (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `events`
--
ALTER TABLE `events`
  MODIFY `event_id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=40;

--
-- AUTO_INCREMENT for table `event_responsibilities`
--
ALTER TABLE `event_responsibilities`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=91;

--
-- AUTO_INCREMENT for table `items`
--
ALTER TABLE `items`
  MODIFY `item_id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=114;

--
-- AUTO_INCREMENT for table `requests`
--
ALTER TABLE `requests`
  MODIFY `request_id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=88;

--
-- AUTO_INCREMENT for table `responsibilities`
--
ALTER TABLE `responsibilities`
  MODIFY `responsibility_id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30;

--
-- AUTO_INCREMENT for table `roles`
--
ALTER TABLE `roles`
  MODIFY `role_id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=251;

--
-- AUTO_INCREMENT for table `user_responsibilities`
--
ALTER TABLE `user_responsibilities`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=61;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `event_responsibilities`
--
ALTER TABLE `event_responsibilities`
  ADD CONSTRAINT `FK_event_responsibility_event` FOREIGN KEY (`event_id`) REFERENCES `events` (`event_id`),
  ADD CONSTRAINT `FK_event_responsibility_responsibility` FOREIGN KEY (`responsibility_id`) REFERENCES `responsibilities` (`responsibility_id`);

--
-- Constraints for table `items`
--
ALTER TABLE `items`
  ADD CONSTRAINT `FK_item_responsibility` FOREIGN KEY (`responsibility_id`) REFERENCES `responsibilities` (`responsibility_id`),
  ADD CONSTRAINT `FK_item_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `requests`
--
ALTER TABLE `requests`
  ADD CONSTRAINT `FK_request_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`),
  ADD CONSTRAINT `FK_request_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `FK_user_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`);

--
-- Constraints for table `user_responsibilities`
--
ALTER TABLE `user_responsibilities`
  ADD CONSTRAINT `FK_user_responsibility_responsibility` FOREIGN KEY (`responsibility_id`) REFERENCES `responsibilities` (`responsibility_id`),
  ADD CONSTRAINT `FK_user_responsibility_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
