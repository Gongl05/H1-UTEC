package com.tuckersoft.branchengine.service;

import java.text.Normalizer;

public class DecisionClassifier {

    public static String normalize(String rawInput) {
        if (rawInput == null) return "";
        return Normalizer
                .normalize(rawInput, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

    public static String classify(String rawInput) {
        String normalized = normalize(rawInput);

        // Regla 1: No contiene ninguna letra de la 'a' a la 'z'
        if (!normalized.matches(".*[a-z].*")) {
            return "ENTRADA_CORRUPTA";
        }

        // Regla 2: Contiene netflix, camara, espectador o videojuego
        if (containsAny(normalized, "netflix", "camara", "espectador", "videojuego")) {
            return "RUPTURA_CUARTA_PARED";
        }

        // Regla 3: Contiene vigilan, simbolo o conspiracion
        if (containsAny(normalized, "vigilan", "simbolo", "conspiracion")) {
            return "SOSPECHA";
        }

        // Regla 4: Contiene rechaza, destruye, desobedece o renuncia
        if (containsAny(normalized, "rechaza", "destruye", "desobedece", "renuncia")) {
            return "REBELDIA";
        }

        // Regla 5: Cualquier otro caso
        return "OBEDIENCIA";
    }

    public static String deriveHandlerUnit(String branchType) {
        return switch (branchType) {
            case "OBEDIENCIA" -> "Mesa de Guion";
            case "REBELDIA" -> "Control de Continuidad";
            case "SOSPECHA" -> "Oficina de Seguridad";
            case "RUPTURA_CUARTA_PARED" -> "Departamento Netflix";
            case "ENTRADA_CORRUPTA" -> "Archivo de Errores";
            default -> "Desconocido";
        };
    }

    public static String deriveOutcomeCode(String branchType) {
        return switch (branchType) {
            case "OBEDIENCIA" -> "ADVANCE_MAIN_PATH";
            case "REBELDIA" -> "FORK_TIMELINE";
            case "SOSPECHA" -> "INJECT_WHITE_BEAR_SYMBOL";
            case "RUPTURA_CUARTA_PARED" -> "BREAK_FOURTH_WALL";
            case "ENTRADA_CORRUPTA" -> "DISCARD_INPUT";
            default -> "UNKNOWN";
        };
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) {
                return true;
            }
        }
        return false;
    }
}
