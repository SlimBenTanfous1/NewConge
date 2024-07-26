-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : jeu. 25 juil. 2024 à 20:56
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `bfpmeconge`
--

-- --------------------------------------------------------

--
-- Structure de la table `conge`
--

CREATE TABLE `conge` (
  `ID_Conge` int(11) NOT NULL,
  `DateDebut` date NOT NULL,
  `DateFin` date NOT NULL,
  `TypeConge` int(11) DEFAULT NULL,
  `Statut` enum('Approuvé','Rejeté','En_Attente') NOT NULL,
  `file` varchar(255) NOT NULL,
  `description` varchar(10000) NOT NULL,
  `ID_User` int(11) NOT NULL,
  `Message` longtext DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `departement`
--

CREATE TABLE `departement` (
  `ID_Departement` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `Parent_Dept` int(11) DEFAULT NULL,
  `Level` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `departement`
--

INSERT INTO `departement` (`ID_Departement`, `nom`, `description`, `Parent_Dept`, `Level`) VALUES
(1, 'Security', 'l\'étude des besoins de sécurité et de confidentialité.', 10, 2),
(2, 'IT_Security', 'Les systèmes informatiques et de communication.', 1, 3),
(3, 'Data_Security', 'Les sécurite des données.', 1, 3),
(4, 'Finance', 'Suivi des coûts.', 10, 2),
(5, 'Bank_Finance', 'La supervision de la comptabilité.', 4, 3),
(6, 'Data_Finance', 'La création de rapports.', 4, 3),
(7, 'RH', 'Ressource Humaines.', 10, 2),
(10, 'Direction générale', 'Departement Parent', NULL, 1),
(11, 'Service finance', 'Service', 6, 4),
(27, 'Service Security', 'lalala', 2, 4);

-- --------------------------------------------------------

--
-- Structure de la table `email_templates`
--

CREATE TABLE `email_templates` (
  `id_Email` int(11) NOT NULL,
  `object` longtext DEFAULT NULL,
  `message` longtext DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `email_templates`
--

INSERT INTO `email_templates` (`id_Email`, `object`, `message`) VALUES
(1, 'Proposition d\'Alternative', 'Cher/Chère [Nom Prenom],\r\n\r\nJe vous écris concernant votre demande de congé pour la période du [DateDebut] au [DateFin]. Malheureusement, en raison des exigences actuelles de notre département, je ne suis pas en mesure d\'approuver votre demande de congé pour les dates spécifiées.\r\n\r\nCependant, je vous propose d\'envisager de prendre vos congés à une autre date ultérieure. Je suis à votre disposition pour discuter de nouvelles dates qui conviendraient à la fois à vos besoins et aux exigences de notre département.\r\n\r\nJe vous remercie de votre compréhension et reste à votre disposition pour toute question.\r\n\r\nCordialement,\r\n\r\n[Nom Prenom Manager]\r\n[Role Manager]'),
(2, 'Équité et Équilibre', 'Cher/Chère [Nom Prenom],\n\nJe vous informe que votre demande de congé pour la période du [DateDebut] au [DateFin] ne peut pas être approuvée à ce moment. Plusieurs de vos collègues ont également demandé des congés pendant cette période, et afin de maintenir un équilibre et une équité au sein de l\'équipe, je ne peux malheureusement pas autoriser votre demande.\n\nJe vous encourage à soumettre une nouvelle demande pour une période différente. N\'hésitez pas à me consulter pour trouver un moment qui conviendrait à la fois à vos besoins et à ceux de l\'équipe.\nMerci de votre compréhension.\n\nCordialement,\n\n[Nom Prenom Manager]\n[Role Manager]'),
(3, 'Politique de Rotation', 'Cher/Chère [Nom Prenom],\r\n\r\nVotre demande de congé pour la période du [DateDebut] au [DateFin] ne peut être approuvée cette fois-ci. Conformément à notre politique de rotation des congés, nous devons nous assurer que chaque membre de l\'équipe a la possibilité de prendre des congés pendant les périodes de vacances populaires.\r\n\r\nJe vous propose de replanifier vos congés à une autre période. Vous pouvez consulter le calendrier des congés disponibles et soumettre une nouvelle demande.\r\n\r\nMerci de votre compréhension et de votre coopération.\r\n\r\nCordialement,\r\n\r\n[Nom Prenom Manager]\r\n[Role Manager]'),
(4, 'Congés Cumulés Non Autorisés', 'Cher/Chère [Nom Prenom],\r\n\r\nJe vous écris pour vous informer que votre demande de congé pour la période du [DateDebut] au [DateFin] ne peut pas être approuvée. Selon la politique de notre entreprise, les congés cumulés au-delà de %d jours ne sont pas autorisés.\r\n\r\nJe vous invite à soumettre une nouvelle demande respectant cette limite. Si vous avez des questions concernant notre politique de congés, je suis disponible pour en discuter.\r\n\r\nMerci de votre compréhension.\r\n\r\nCordialement,\r\n\r\n[Nom Prenom Manager]\r\n[Role Manager]'),
(5, 'Remplacement Non Disponible', 'Cher/Chère [Nom Prenom],\r\n\r\nJe dois malheureusement refuser votre demande de congé pour la période du [DateDebut] au [DateFin]. À l\'heure actuelle, il n\'y a pas de remplaçant disponible pour couvrir vos tâches pendant votre absence.\r\n\r\nJe vous encourage à proposer une autre période pour vos congés, et nous ferons de notre mieux pour organiser un remplacement adéquat.\r\n\r\nMerci de votre compréhension et de votre coopération.\r\n\r\nCordialement,\r\n\r\n[Nom Prenom Manager]\r\n[Role Manager]'),
(6, 'Évaluation de Performance ou Audit', 'Cher/Chère [Nom Prenom],\r\n\r\nJe vous écris pour vous informer que votre demande de congé pour la période du [DateDebut] au [DateFin] ne peut pas être approuvée. Pendant cette période, nous avons planifié une évaluation de performance/audit important(e) pour notre département, et votre présence est nécessaire.\r\n\r\nJe vous invite à soumettre une nouvelle demande de congé pour une période ultérieure. Je reste à votre disposition pour toute question ou pour discuter des dates alternatives possibles.\r\n\r\nMerci de votre compréhension.\r\n\r\nCordialement,\r\n\r\n[Nom Prenom Manager]\r\n[Role Manager]'),
(7, 'Opérationnelles et Organisationnelles', 'Cher/Chère [Nom Prenom],\r\n\r\nJe vous écris pour vous informer que votre demande de congé pour la période du [DateDebut] au [DateFin] ne peut pas être approuvée pour des raisons opérationnelles et organisationnelles.\r\n\r\nNous comprenons l\'importance de vos besoins personnels et nous regrettons de ne pouvoir accéder à votre demande à ce moment. Nous vous encourageons à soumettre une nouvelle demande de congé pour une période ultérieure qui pourrait mieux convenir aux besoins opérationnels de notre département.\r\n\r\nNous restons à votre disposition pour toute question ou pour discuter des dates alternatives possibles.\r\n\r\nMerci de votre compréhension.\r\n\r\nCordialement,\r\n\r\n[Nom Prenom Manager]\r\n[Role Manager]'),
(8, 'Apprové', 'Cher/Chère [Nom Prenom],\r\n\r\nJe vous écris pour vous informer que votre demande de congé pour la période du [DateDebut] au [DateFin] a été approuvée.\r\n\r\nNous vous souhaitons une période de congé agréable et reposante. Si vous avez besoin de quoi que ce soit avant votre départ, n\'hésitez pas à me contacter.\r\n\r\nMerci de votre travail acharné et de votre dévouement.\r\n\r\nCordialement,\r\n\r\n[Nom Prenom Manager]\r\n[Role Manager]');

-- --------------------------------------------------------

--
-- Structure de la table `notification`
--

CREATE TABLE `notification` (
  `ID_Notif` int(11) NOT NULL,
  `ID_User` int(11) NOT NULL,
  `NotfiMessage` mediumtext NOT NULL,
  `NotifContent` mediumtext DEFAULT NULL,
  `Statut` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `notification`
--

INSERT INTO `notification` (`ID_Notif`, `ID_User`, `NotfiMessage`, `NotifContent`, `Statut`) VALUES
(10, 1, 'Vous avez reçu une nouvelle demande de congé Annuel', 'Vous avez reçu une nouvelle demande de congé Annuel de la part de Youssef Youssef du 2024-07-18 au 2024-07-19', 2),
(11, 1, 'Vous avez reçu une nouvelle demande de congé Annuel', 'Vous avez reçu une nouvelle demande de congé Annuel de la part de Youssef Youssef du 2024-07-18 au 2024-07-19', 2);

-- --------------------------------------------------------

--
-- Structure de la table `role`
--

CREATE TABLE `role` (
  `ID_Role` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  `Level` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `role`
--

INSERT INTO `role` (`ID_Role`, `nom`, `description`, `Level`) VALUES
(1, 'DG', 'President-Director General role with full permissions', 1),
(2, 'Directeur', 'Director role with managerial permissions', 2),
(3, 'Sous_Directeur', 'Assistant Director role with limited managerial permissions', 3),
(4, 'Chef_Service', 'Service Head role with department permissions', 4),
(5, 'Employe', 'Employee role with basic permissions', NULL),
(7, 'RH', 'xD au Carré', NULL),
(8, 'AdminIT', 'AdminIT with all priveleges .', NULL),
(10, 'Gustavo', 'Pour tester', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `rolehierarchie`
--

CREATE TABLE `rolehierarchie` (
  `ID_RoleH` int(11) NOT NULL,
  `ID_RoleP` int(11) NOT NULL,
  `ID_RoleC` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `rolehierarchie`
--

INSERT INTO `rolehierarchie` (`ID_RoleH`, `ID_RoleP`, `ID_RoleC`) VALUES
(1, 1, 2),
(5, 2, 5),
(8, 3, 5),
(9, 3, 4),
(10, 2, 3),
(11, 4, 5),
(12, 1, 4),
(13, 2, 4),
(14, 3, 4),
(16, 1, 3),
(17, 1, 5),
(18, 2, 10);

-- --------------------------------------------------------

--
-- Structure de la table `role_departement`
--

CREATE TABLE `role_departement` (
  `ID_RoleDep` int(11) NOT NULL,
  `ID_Role` int(11) DEFAULT NULL,
  `ID_Departement` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `role_departement`
--

INSERT INTO `role_departement` (`ID_RoleDep`, `ID_Role`, `ID_Departement`) VALUES
(1, 1, 10),
(2, 2, 7),
(3, 2, 4),
(4, 2, 1),
(5, 3, 3),
(6, 3, 6),
(7, 3, 2),
(8, 3, 5),
(9, 4, 11),
(10, 4, 27);

-- --------------------------------------------------------

--
-- Structure de la table `typeconge`
--

CREATE TABLE `typeconge` (
  `ID_TypeConge` int(11) NOT NULL,
  `Designation` varchar(255) DEFAULT NULL,
  `Pas` double DEFAULT NULL,
  `PeriodeJ` int(11) DEFAULT NULL,
  `PeriodeM` int(11) DEFAULT NULL,
  `PeriodeA` int(11) DEFAULT NULL,
  `File` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

--
-- Déchargement des données de la table `typeconge`
--

INSERT INTO `typeconge` (`ID_TypeConge`, `Designation`, `Pas`, `PeriodeJ`, `PeriodeM`, `PeriodeA`, `File`) VALUES
(18, 'Annuel', 30, 0, 0, 1, 0),
(19, 'Maladie', 3, 0, 1, 0, 1),
(20, 'Maternité', 2, 0, 1, 0, 1),
(21, 'Sous Solde', 14, 0, 0, 1, 0);

-- --------------------------------------------------------

--
-- Structure de la table `user`
--

CREATE TABLE `user` (
  `ID_User` int(11) NOT NULL,
  `Nom` varchar(255) NOT NULL,
  `Prenom` varchar(255) NOT NULL,
  `Email` varchar(255) NOT NULL,
  `MDP` varchar(255) NOT NULL,
  `Image` varchar(255) NOT NULL,
  `ID_Departement` int(11) DEFAULT NULL,
  `ID_Manager` int(11) DEFAULT NULL,
  `Creation_Date` date DEFAULT NULL,
  `idSolde` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `user`
--

INSERT INTO `user` (`ID_User`, `Nom`, `Prenom`, `Email`, `MDP`, `Image`, `ID_Departement`, `ID_Manager`, `Creation_Date`, `idSolde`) VALUES
(1, 'Chouaibb', 'Slims', '222', '222', 'src\\main\\resources\\assets\\imgs\\4d58a9dd-89bc-49c8-b9b2-f1a0d59b5c5f_347577815_6341761682604334_4518175521665308777_n.jpg', NULL, NULL, NULL, NULL),
(2, 'Ala', 'Moussa', 'ALA', 'ALA', 'src\\main\\resources\\assets\\imgs\\4d58a9dd-89bc-49c8-b9b2-f1a0d59b5c5f_347577815_6341761682604334_4518175521665308777_n.jpg', 1, 1, NULL, NULL),
(3, 'Flen', 'Foulen', '111', '111', 'src\\main\\resources\\assets\\imgs\\4d58a9dd-89bc-49c8-b9b2-f1a0d59b5c5f_347577815_6341761682604334_4518175521665308777_n.jpg', 2, 2, NULL, NULL),
(11, 'Mgaidi', 'Rami', '1', '1', 'src\\main\\resources\\assets\\imgs\\4d58a9dd-89bc-49c8-b9b2-f1a0d59b5c5f_347577815_6341761682604334_4518175521665308777_n.jpg', 27, 18, NULL, NULL),
(18, 'Youssef', 'Youssef', '999', '999', 'src\\main\\resources\\assets\\imgs\\4d58a9dd-89bc-49c8-b9b2-f1a0d59b5c5f_347577815_6341761682604334_4518175521665308777_n.jpg', 7, 3, NULL, NULL),
(23, 'John', 'Doe', 'john.doe@example.com', 'password', 'image.png', 4, 1, '2024-07-09', NULL),
(29, 'Ferah', 'Ilyes', '21', '21', '', 11, 23, '2024-07-18', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `user_role`
--

CREATE TABLE `user_role` (
  `ID_User` int(11) NOT NULL,
  `ID_Role` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `user_role`
--

INSERT INTO `user_role` (`ID_User`, `ID_Role`) VALUES
(1, 1),
(2, 2),
(3, 3),
(11, 5),
(18, 2),
(23, 2),
(29, 4);

-- --------------------------------------------------------

--
-- Structure de la table `user_solde`
--

CREATE TABLE `user_solde` (
  `ID_UserSolde` int(11) NOT NULL,
  `ID_User` int(11) NOT NULL,
  `ID_TypeConge` int(11) NOT NULL,
  `TotalSolde` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `user_solde`
--

INSERT INTO `user_solde` (`ID_UserSolde`, `ID_User`, `ID_TypeConge`, `TotalSolde`) VALUES
(115, 1, 18, 10),
(116, 2, 18, 0),
(117, 3, 18, 0),
(118, 23, 18, 0),
(119, 18, 18, 10),
(120, 29, 18, 0),
(121, 11, 18, 0),
(122, 1, 19, 0),
(123, 2, 19, 0),
(124, 3, 19, 0),
(125, 23, 19, 0),
(126, 18, 19, 0),
(127, 29, 19, 0),
(128, 11, 19, 0),
(129, 1, 20, 0),
(130, 2, 20, 0),
(131, 3, 20, 0),
(132, 23, 20, 0),
(133, 18, 20, 0),
(134, 29, 20, 0),
(135, 11, 20, 0),
(136, 1, 21, 0),
(137, 2, 21, 0),
(138, 3, 21, 0),
(139, 23, 21, 0),
(140, 18, 21, 0),
(141, 29, 21, 0),
(142, 11, 21, 0);

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `conge`
--
ALTER TABLE `conge`
  ADD PRIMARY KEY (`ID_Conge`),
  ADD KEY `idx_conge_user` (`ID_User`),
  ADD KEY `fk_type` (`TypeConge`);

--
-- Index pour la table `departement`
--
ALTER TABLE `departement`
  ADD PRIMARY KEY (`ID_Departement`),
  ADD KEY `idx_departement_parent` (`Parent_Dept`);

--
-- Index pour la table `email_templates`
--
ALTER TABLE `email_templates`
  ADD PRIMARY KEY (`id_Email`);

--
-- Index pour la table `notification`
--
ALTER TABLE `notification`
  ADD PRIMARY KEY (`ID_Notif`),
  ADD KEY `fk_notification_iduser` (`ID_User`) USING BTREE;

--
-- Index pour la table `role`
--
ALTER TABLE `role`
  ADD PRIMARY KEY (`ID_Role`);

--
-- Index pour la table `rolehierarchie`
--
ALTER TABLE `rolehierarchie`
  ADD PRIMARY KEY (`ID_RoleH`),
  ADD KEY `idx_rolehierarchie_rolep` (`ID_RoleP`),
  ADD KEY `idx_rolehierarchie_rolec` (`ID_RoleC`);

--
-- Index pour la table `role_departement`
--
ALTER TABLE `role_departement`
  ADD PRIMARY KEY (`ID_RoleDep`),
  ADD KEY `ID_Role` (`ID_Role`),
  ADD KEY `ID_Departement` (`ID_Departement`);

--
-- Index pour la table `typeconge`
--
ALTER TABLE `typeconge`
  ADD PRIMARY KEY (`ID_TypeConge`);

--
-- Index pour la table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`ID_User`),
  ADD KEY `idx_user_dept` (`ID_Departement`),
  ADD KEY `idx_user_dept1` (`ID_Departement`),
  ADD KEY `FK__user_manager1` (`ID_Manager`);

--
-- Index pour la table `user_role`
--
ALTER TABLE `user_role`
  ADD PRIMARY KEY (`ID_User`,`ID_Role`),
  ADD KEY `idx_user_role_user` (`ID_User`),
  ADD KEY `idx_user_role_role` (`ID_Role`);

--
-- Index pour la table `user_solde`
--
ALTER TABLE `user_solde`
  ADD PRIMARY KEY (`ID_UserSolde`),
  ADD KEY `user_solde_ibfk_1` (`ID_User`),
  ADD KEY `user_solde_ibfk_2` (`ID_TypeConge`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `conge`
--
ALTER TABLE `conge`
  MODIFY `ID_Conge` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT pour la table `departement`
--
ALTER TABLE `departement`
  MODIFY `ID_Departement` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=42;

--
-- AUTO_INCREMENT pour la table `email_templates`
--
ALTER TABLE `email_templates`
  MODIFY `id_Email` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT pour la table `notification`
--
ALTER TABLE `notification`
  MODIFY `ID_Notif` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT pour la table `role`
--
ALTER TABLE `role`
  MODIFY `ID_Role` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT pour la table `rolehierarchie`
--
ALTER TABLE `rolehierarchie`
  MODIFY `ID_RoleH` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=31;

--
-- AUTO_INCREMENT pour la table `role_departement`
--
ALTER TABLE `role_departement`
  MODIFY `ID_RoleDep` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT pour la table `typeconge`
--
ALTER TABLE `typeconge`
  MODIFY `ID_TypeConge` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT pour la table `user`
--
ALTER TABLE `user`
  MODIFY `ID_User` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30;

--
-- AUTO_INCREMENT pour la table `user_solde`
--
ALTER TABLE `user_solde`
  MODIFY `ID_UserSolde` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=143;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `conge`
--
ALTER TABLE `conge`
  ADD CONSTRAINT `fk_conge_user` FOREIGN KEY (`ID_User`) REFERENCES `user` (`ID_User`),
  ADD CONSTRAINT `fk_type` FOREIGN KEY (`TypeConge`) REFERENCES `typeconge` (`ID_TypeConge`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_user1` FOREIGN KEY (`ID_User`) REFERENCES `user` (`ID_User`);

--
-- Contraintes pour la table `departement`
--
ALTER TABLE `departement`
  ADD CONSTRAINT `fk_departement_parent` FOREIGN KEY (`Parent_Dept`) REFERENCES `departement` (`ID_Departement`);

--
-- Contraintes pour la table `notification`
--
ALTER TABLE `notification`
  ADD CONSTRAINT `fk_notification_iduser` FOREIGN KEY (`ID_User`) REFERENCES `user` (`ID_User`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `rolehierarchie`
--
ALTER TABLE `rolehierarchie`
  ADD CONSTRAINT `fk_rolehierarchie_rolec` FOREIGN KEY (`ID_RoleC`) REFERENCES `role` (`ID_Role`),
  ADD CONSTRAINT `fk_rolehierarchie_rolep` FOREIGN KEY (`ID_RoleP`) REFERENCES `role` (`ID_Role`);

--
-- Contraintes pour la table `role_departement`
--
ALTER TABLE `role_departement`
  ADD CONSTRAINT `role_departement_ibfk_1` FOREIGN KEY (`ID_Role`) REFERENCES `role` (`ID_Role`),
  ADD CONSTRAINT `role_departement_ibfk_2` FOREIGN KEY (`ID_Departement`) REFERENCES `departement` (`ID_Departement`);

--
-- Contraintes pour la table `user`
--
ALTER TABLE `user`
  ADD CONSTRAINT `FK__user_manager1` FOREIGN KEY (`ID_Manager`) REFERENCES `user` (`ID_User`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_user_dept1` FOREIGN KEY (`ID_Departement`) REFERENCES `departement` (`ID_Departement`);

--
-- Contraintes pour la table `user_role`
--
ALTER TABLE `user_role`
  ADD CONSTRAINT `fk_role` FOREIGN KEY (`ID_Role`) REFERENCES `role` (`ID_Role`),
  ADD CONSTRAINT `fk_user` FOREIGN KEY (`ID_User`) REFERENCES `user` (`ID_User`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_user_role_role` FOREIGN KEY (`ID_Role`) REFERENCES `role` (`ID_Role`),
  ADD CONSTRAINT `fk_user_role_user` FOREIGN KEY (`ID_User`) REFERENCES `user` (`ID_User`),
  ADD CONSTRAINT `user_role_ibfk_1` FOREIGN KEY (`ID_User`) REFERENCES `user` (`ID_User`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `user_role_ibfk_2` FOREIGN KEY (`ID_Role`) REFERENCES `role` (`ID_Role`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `user_solde`
--
ALTER TABLE `user_solde`
  ADD CONSTRAINT `user_solde_ibfk_1` FOREIGN KEY (`ID_User`) REFERENCES `user` (`ID_User`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `user_solde_ibfk_2` FOREIGN KEY (`ID_TypeConge`) REFERENCES `typeconge` (`ID_TypeConge`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
