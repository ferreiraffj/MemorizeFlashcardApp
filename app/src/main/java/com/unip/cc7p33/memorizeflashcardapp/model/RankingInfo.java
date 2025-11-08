package com.unip.cc7p33.memorizeflashcardapp.model;

public class RankingInfo {
    private final String currentRankName;
    private final String nextRankName;
    private final int currentRankXp;
    private final int nextRankXp;
    private final int userXp;

    // Correção: Parêntese ')' no final da declaração dos parâmetros
    public RankingInfo(String currentRankName, String nextRankName, int currentRankXp, int nextRankXp, int userXp) {
        this.currentRankName = currentRankName;
        this.nextRankName = nextRankName;
        this.currentRankXp = currentRankXp;
        this.nextRankXp = nextRankXp;
        this.userXp = userXp;
    }

    public String getCurrentRankName() { return currentRankName; }
    public String getNextRankName() { return nextRankName; }
    public int getCurrentRankXp() { return currentRankXp; }
    public int getNextRankXp() { return nextRankXp; }
    public int getUserXp() { return userXp; }


    public int getProgressPercentage() {
        // Se já está no ranking máximo, a barra fica cheia.
        if (nextRankXp <= currentRankXp) return 100;

        int totalXpForLevel = nextRankXp - currentRankXp;
        int xpIntoLevel = userXp - currentRankXp;

        // Evita divisão por zero se os valores forem iguais por algum motivo.
        if (totalXpForLevel == 0) return 100;

        // Casting para double para garantir a precisão do cálculo antes de converter para int.
        return (int) (((double) xpIntoLevel / totalXpForLevel) * 100);
    }
}
