-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 16-05-2026 a las 21:08:14
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
-- Base de datos: `db_pedido`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `detalle_pedido`
--

CREATE TABLE `detalle_pedido` (
  `id` bigint(20) NOT NULL,
  `cantidad` int(11) DEFAULT NULL,
  `precio_unitario` double DEFAULT NULL,
  `producto_id` bigint(20) DEFAULT NULL,
  `subtotal` double DEFAULT NULL,
  `pedido_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `detalle_pedido`
--

INSERT INTO `detalle_pedido` (`id`, `cantidad`, `precio_unitario`, `producto_id`, `subtotal`, `pedido_id`) VALUES
(3, 2, 30000, 1, 60000, 1),
(4, 6, 30000, 1, 180000, 2),
(5, 6, 30000, 1, 180000, 3),
(6, 20, 25000, 2, 500000, 3),
(7, 30, 60000, 3, 1800000, 4),
(8, 10, 25000, 2, 250000, 4),
(11, 20, 30000, 1, 600000, 5),
(12, 15, 25000, 2, 375000, 5),
(15, 20, 30000, 1, 600000, 6),
(16, 15, 25000, 2, 375000, 6),
(19, 20, 30000, 1, 600000, 7),
(20, 15, 25000, 2, 375000, 7),
(24, 20, 30000, 1, 600000, 8),
(25, 40, 25000, 2, 1000000, 8),
(26, 15, 60000, 3, 900000, 8),
(27, 20, 30000, 1, 600000, 9),
(28, 15, 25000, 2, 375000, 9),
(29, 20, 30000, 1, 600000, 10),
(30, 10, 25000, 2, 250000, 10),
(31, 20, 30000, 1, 600000, 11),
(32, 10, 25000, 2, 250000, 11),
(33, 20, 30000, 1, 600000, 12),
(34, 15, 25000, 2, 375000, 12);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pedido`
--

CREATE TABLE `pedido` (
  `id` bigint(20) NOT NULL,
  `cantidad` int(11) DEFAULT NULL,
  `cliente_id` bigint(20) DEFAULT NULL,
  `estado` varchar(255) DEFAULT NULL,
  `fecha_pedido` date DEFAULT NULL,
  `producto_id` bigint(20) DEFAULT NULL,
  `total_pago` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `pedido`
--

INSERT INTO `pedido` (`id`, `cantidad`, `cliente_id`, `estado`, `fecha_pedido`, `producto_id`, `total_pago`) VALUES
(1, NULL, 1, 'FINALIZADO', '2026-05-15', NULL, 60000),
(2, NULL, 1, 'FINALIZADO', '2026-05-15', NULL, 180000),
(3, NULL, 2, 'PENDIENTE', '2026-05-15', NULL, 680000),
(4, NULL, 4, 'FINALIZADO', '2026-05-16', NULL, 2050000),
(5, NULL, 1, 'FINALIZADO', '2026-05-16', NULL, 975000),
(6, NULL, 1, 'FINALIZADO', '2026-05-16', NULL, 975000),
(7, NULL, 1, 'FINALIZADO', '2026-05-16', NULL, 975000),
(8, NULL, 1, 'FINALIZADO', '2026-05-16', NULL, 2500000),
(9, NULL, 1, 'PENDIENTE', '2026-05-16', NULL, 975000),
(10, NULL, 1, 'PENDIENTE', '2026-05-16', NULL, 850000),
(11, NULL, 1, 'FINALIZADO', '2026-05-16', NULL, 850000),
(12, NULL, 1, 'FINALIZADO', '2026-05-16', NULL, 975000);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `detalle_pedido`
--
ALTER TABLE `detalle_pedido`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKgqvba9e7dildyw45u0usdj1k2` (`pedido_id`);

--
-- Indices de la tabla `pedido`
--
ALTER TABLE `pedido`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `detalle_pedido`
--
ALTER TABLE `detalle_pedido`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=35;

--
-- AUTO_INCREMENT de la tabla `pedido`
--
ALTER TABLE `pedido`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `detalle_pedido`
--
ALTER TABLE `detalle_pedido`
  ADD CONSTRAINT `FKgqvba9e7dildyw45u0usdj1k2` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
