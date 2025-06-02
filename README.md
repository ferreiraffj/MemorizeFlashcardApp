# Memorize - Flashcards App

**Um aplicativo móvel para memorização ativa com repetição espaçada e gamificação**

---

## :dart: Objetivo  
Desenvolver uma ferramenta de estudos baseada em flashcards que:  
- Otimiza a memorização com **algoritmo SM-2** (repetição espaçada)  
- Aumenta o engajamento através de **níveis, XP e rankings**  
- Funciona offline e sincroniza dados via Firebase  

---

## :iphone: Funcionalidades Principais  

| Feature               | Descrição                                                                 | Status       |  
|-----------------------|---------------------------------------------------------------------------|--------------|  
| :busts_in_silhouette: Autenticação | Login com e-mail/Firebase Auth                                          | ✔️ Implementado |  
| :notebook: Baralhos   | Crie e organize flashcards por matéria/tema                              | 🚧 Em progresso |  
| :game_die: Gamificação| Ganhe XP, suba de nível e desbloqueie conquistas                         | 🚧 Em progresso |  
| :chart_with_upwards_trend: Estatísticas | Visualize acertos, erros e tempo médio por baralho           | 🚧 Em progresso |  
| :bell: Notificações   | Lembretes para revisões pendentes                                        | ✅ Planejado   |  

---

## :wrench: Tecnologias  

| Área           | Tecnologias                                                                 |  
|----------------|-----------------------------------------------------------------------------|  
| **Frontend**   | Android Studio (Java), XML, Jetpack Compose                               |  
| **Backend**    | Firebase (Auth, Firestore, Cloud Functions)                                 |  
| **Bibliotecas**| MPAndroidChart, WorkManager, Gson                                           |  

---

## :open_file_folder: Estrutura do Projeto  

```javascript
flashmaster/
├── app/
│ ├── src/main/
│ │ ├── java/com/flashmaster/
│ │ │ ├── models/ # Classes de dados (Usuario, Baralho, Flashcard, Desempenho)
│ │ │ ├── services/ # Lógica de negócio (AuthService, BaralhoService, FlashcardService, GamificacaoService)
│ │ │ ├── views/ # Activities e Fragments
│ │ │ └── adapters/ # RecyclerView.Adapters
│ │ └── res/ # Layouts, strings, ícones
├── README.md
└── build.gradle
```

:page_facing_up: Licença
Este projeto está sob licença MIT [Clique para detalhes.](https://choosealicense.com/licenses/mit/) 
