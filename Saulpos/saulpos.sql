-- phpMyAdmin SQL Dump
-- version 3.5.1
-- http://www.phpmyadmin.net
--
-- Servidor: instance25380.db.xeround.com.:16645
-- Tiempo de generación: 02-09-2012 a las 23:43:45
-- Versión del servidor: 5.1.42
-- Versión de PHP: 5.4.3

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Base de datos: `saulpos`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `configuration`
--

CREATE TABLE IF NOT EXISTS `configuration` (
  `SectionName` varchar(10) COLLATE utf8_bin NOT NULL COMMENT 'Section of the configuration',
  `SettingName` varchar(20) COLLATE utf8_bin NOT NULL COMMENT 'Name of the configuration (KEY)',
  `SettingValue` varchar(1000) COLLATE utf8_bin DEFAULT NULL COMMENT 'Value',
  `SettingType` tinyint(4) NOT NULL COMMENT '0 = String , 1 = Integer, 2 = Double',
  PRIMARY KEY (`SectionName`,`SettingName`)
) ENGINE=Xeround DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Configuration Table';

--
-- Volcado de datos para la tabla `configuration`
--

INSERT INTO `configuration` (`SectionName`, `SettingName`, `SettingValue`, `SettingType`) VALUES
('Company', 'companyName', 'Saul Pos', 0);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `node`
--

CREATE TABLE IF NOT EXISTS `node` (
  `id` varchar(15) COLLATE utf8_bin NOT NULL COMMENT 'Node''s ID',
  `caption` varchar(50) COLLATE utf8_bin NOT NULL COMMENT 'Description',
  `predecessor` varchar(15) COLLATE utf8_bin NOT NULL COMMENT 'Pather in the graph',
  `icon` blob NOT NULL COMMENT 'Image',
  `function` varchar(20) COLLATE utf8_bin NOT NULL COMMENT 'Function called in this menu',
  `administrative` tinyint(1) NOT NULL COMMENT 'True if and only if it is an administrative menu',
  PRIMARY KEY (`id`),
  KEY `function` (`function`)
) ENGINE=Xeround DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Navegation Table';

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `profile`
--

CREATE TABLE IF NOT EXISTS `profile` (
  `id` varchar(15) COLLATE utf8_bin NOT NULL COMMENT 'Id''s Profile',
  `description` varchar(150) COLLATE utf8_bin DEFAULT NULL COMMENT 'Description''s Profile',
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=MyIsam DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='User''s profile';

--
-- Volcado de datos para la tabla `profile`
--

INSERT INTO `profile` (`id`, `description`, `timestamp`) VALUES
('4', '11', '2012-09-02 19:19:54'),
('6', '6', '2012-09-02 19:20:05'),
('1', '55515551', '2012-09-02 19:16:48'),
('22', '155', '0000-00-00 00:00:00');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `user`
--

CREATE TABLE IF NOT EXISTS `user` (
  `id` varchar(15) COLLATE utf8_bin NOT NULL COMMENT 'User''s Identifier',
  `password` varchar(50) COLLATE utf8_bin NOT NULL COMMENT 'User''s password',
  `name` varchar(50) COLLATE utf8_bin NOT NULL COMMENT 'User''s Name',
  `profile_id` varchar(15) COLLATE utf8_bin NOT NULL COMMENT 'Profile',
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=Xeround DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='User''s Table';

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
