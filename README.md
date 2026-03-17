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
| :notebook: Baralhos   | Crie e organize flashcards por matéria/tema                              | ✔️ Implementado |  
| :game_die: Gamificação| Ganhe XP, suba de nível e desbloqueie conquistas                         | ✔️ Implementado |  
| :chart_with_upwards_trend: Estatísticas | Visualize acertos, erros e tempo médio por baralho           | ✔️ Implementado |  
| :bell: Notificações   | Lembretes para revisões pendentes                                        | ✔️ Implementado |  

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
memorizeflashcardaapp/
├── app/
│ ├── src/main/
│ │ ├── java/com/flashmaster/
│ │ │ ├── adapter/ # RecyclerView.Adapters
│ │ │ ├── database/ # AppDatabase e dados dos models
│ │ │ ├── model/ # Classes de dados (Usuario, Baralho, Flashcard, Desempenho)
│ │ │ ├── repository
│ │ │ ├── service/ # Lógica de negócio (AuthService, BaralhoService, FlashcardService, GamificacaoService)
│ │ │ ├── utils/ # Utilitários (Funções de UI)
│ │ │ └── view/ # Activities e Fragments
│ │ └── res/ # Layouts, strings, ícones
├── README.md
└── build.gradle
```

:page_facing_up: Licença
Este projeto está sob licença MIT [Clique para detalhes.](https://choosealicense.com/licenses/mit/) 

## Manual para contribuição
[MemorizeFlashcardApp - Manual](https://opaque-salute-56f.notion.site/Contribuindo-com-um-projeto-no-github-2283e5e3a05180e8bbacdf5cec395274)
