-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 16-05-2026 a las 20:54:30
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `db_usuario`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuario`
--

CREATE TABLE `usuario` (
  `id` bigint(20) NOT NULL,
  `fecha_registro` date DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `rol` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuario`
--

INSERT INTO `usuario` (`id`, `fecha_registro`, `password`, `rol`, `username`) VALUES
(1, '2026-05-15', '$2a$10$XD0H./XHiOjUXxvYzBtfA.nXzLKwwx0yJ8YQj0KxGuG69U/IWxQzy', 'CLIENTE', 'Diego'),
(3, '2026-05-15', '$2a$10$/VyjVMdPKOV0dtoqb2287OB1pucUc2T1HNzEKWgXGFf1i8ZZ26CxG', 'ADMIN', 'admin'),
(4, '2026-05-15', '$2a$10$RCe68GeBZ13KhiWv5coRXu2flD.0WDK5kJbh9A6.qu2xHyNYmGdAu', 'CLIENTE', 'jorge'),
(5, '2026-05-15', '$2a$10$4VgJwdT5whP7iBENku.dVeffWucbXNFA.HTGsSKZNhd/yvsbsa3E2', 'CLIENTE', 'Dante'),
(9, '2026-05-16', '$2a$10$2xZ5pGH0SapJhhuUf7mD7ON5KSymycfZqJJ8nJtbBEok.ALpSo5Ui', 'CLIENTE', 'Dante1'),
(10, '2026-05-16', '$2a$10$npCtPxJfJ7SwtlnpZMoDnezDdoyTytv2u8wbD4Oxhx61WsQwYOkdO', 'CLIENTE', 'Dante2'),
(11, '2026-05-16', '$2a$10$65XfDfrZ4R2wDwoy/Bxmp.FKatm2g8tkFIDEvO76gMLKDsnHSy2Te', 'CLIENTE', 'Dante3'),
(12, '2026-05-16', '$2a$10$VJ.Qp3raM/ojYZjsYqcS4ecTlahTnjz6rWp3f2xq75XUtQgZvK4RC', 'CLIENTE', 'Dante5'),
(13, '2026-05-16', '$2a$10$Ldz.u3ORYKCBzugapyZAvO/cRCx9nUrebuS51nhQheU0FDxDdv6HO', 'CLIENTE', 'Dante8'),
(14, '2026-05-16', '$2a$10$G4n2UTbZdjAuqyc9GpPTLuI3yuB.gXqGwUZ4kHpPCoMuf3mPTEntq', 'CLIENTE', 'Dante9'),
(15, '2026-05-16', '$2a$10$EjotGcSlYVvafyyyzoGdee64vu9GCDpfkZRoH/ujoEYhPmk3hwkde', 'CLIENTE', 'Dante10'),
(16, '2026-05-16', '$2a$10$LAeA9tK7UfOvPLb6jTpnMuXHCLPY2.R9WZqWM0iwLTytSrg9ueW8e', 'CLIENTE', 'Dante11'),
(17, '2026-05-16', '$2a$10$QBjfxW0RbvPKqu1rKT8Lh.79nCnPu91u2qTWE.lUxykacHkHgNT.O', 'CLIENTE', 'Dantess'),
(18, '2026-05-16', '$2a$10$8l6VNwKYxGrvrX00O8ymIeb57QTGZrt5GcmfkM3EIhKkY1WdYzgdS', 'CLIENTE', 'Dante74'),
(19, '2026-05-16', '$2a$10$zfJShsA38Gddbz4YrNMqm.vs30S.YCFUUJNsFdHqoocEl0aE2vawC', 'CLIENTE', 'Dante78');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `usuario`
--
ALTER TABLE `usuario`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `usuario`
--
ALTER TABLE `usuario`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
