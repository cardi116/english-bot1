package com.englishbot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Main extends TelegramLongPollingBot {

    private static final String BOT_TOKEN = "8687641805:AAHtGAFO8ZvVMaQMjz16yapwZOQWAwnW9GI";
    private static final String BOT_USERNAME = "@englishstale_bot";

    private Database db;
    private Map<Long, String> trainingSessions;

    public Main() {
        db = new Database();
        trainingSessions = new ConcurrentHashMap<>();
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    // Создание главной клавиатуры
    private ReplyKeyboardMarkup createKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("🌟 Слово дня");
        row1.add("🎲 Случайное слово");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("📖 Словарь");
        row2.add("📝 Мои слова");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("➕ Добавить слово");
        row3.add("🎯 Тренировка");

        KeyboardRow row4 = new KeyboardRow();
        row4.add("📊 Статистика");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    @Override
    public void onUpdateReceived(Update update) {

        // --- 1. НОВЫЙ БЛОК: ОБРАБОТКА НАЖАТИЙ НА КНОПКИ ВИКТОРИНЫ ---
        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setParseMode("Markdown");

            if (data.equals("quiz_correct")) {
                message.setText("✅ *Правильно!* Молодец! 🎉\n\nХочешь продолжить? Нажми кнопку 🎯 Тренировка");
                // Если нужно, здесь можно добавить статистику: db.updateWordStats(userId, word, true);
            } else {
                message.setText("❌ *Неправильно.*\n\nПопробуй еще раз! Нажми кнопку 🎯 Тренировка");
            }
            message.setReplyMarkup(createKeyboard());

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return; // Завершаем метод, чтобы бот не пытался читать текст, которого нет
        }

        // --- 2. СТАРЫЙ БЛОК: ОБРАБОТКА ОБЫЧНОГО ТЕКСТА ---
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();
            String userName = update.getMessage().getFrom().getFirstName();
            String username = update.getMessage().getFrom().getUserName();

            boolean isNewUser = !db.userExists(userId);
            db.registerUser(userId, username, userName);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setParseMode("Markdown");

            if (messageText.equals("🌟 Слово дня")) messageText = "/wordoftheday";
            else if (messageText.equals("🎲 Случайное слово")) messageText = "/random";
            else if (messageText.equals("📖 Словарь")) messageText = "/words";
            else if (messageText.equals("📝 Мои слова")) messageText = "/mywords";
            else if (messageText.equals("➕ Добавить слово")) {
                message.setText("➕ *Добавь новое слово*\n\nВведи команду:\n`/add слово - перевод`\n\nПример: `/add cat - кошка`");
                try { execute(message); } catch (TelegramApiException e) { e.printStackTrace(); }
                return;
            }
            else if (messageText.equals("🎯 Тренировка")) messageText = "/train";
            else if (messageText.equals("📊 Статистика")) messageText = "/stats";

            // Старая логика проверки ответа (ручной ввод), оставляем на всякий случай
            if (trainingSessions.containsKey(userId)) {
                String currentWord = trainingSessions.get(userId);
                String expectedTranslation = db.getTranslation(userId, currentWord);

                if (messageText.equalsIgnoreCase(expectedTranslation)) {
                    message.setText("✅ *Правильно!* Молодец! 🎉\n\nХочешь продолжить? Нажми кнопку 🎯 Тренировка");
                    db.updateWordStats(userId, currentWord, true);
                    trainingSessions.remove(userId);
                    message.setReplyMarkup(createKeyboard());
                } else if (!messageText.startsWith("/") && !messageText.contains("Слово") && !messageText.contains("Словарь") && !messageText.contains("Тренировка") && !messageText.contains("Статистика")) {
                    message.setText("❌ *Неправильно.*\n\nПравильный перевод: *" + expectedTranslation + "*\n\nПопробуй еще раз! Нажми кнопку 🎯 Тренировка");
                    db.updateWordStats(userId, currentWord, false);
                    trainingSessions.remove(userId);
                    message.setReplyMarkup(createKeyboard());
                } else {
                    trainingSessions.remove(userId);
                }
            }

            // Обрабатываем команды
            if (messageText.equals("/start")) {
                message.setText("Привет, " + userName + "! 👋\n\nЯ бот для изучения английского языка!\n\n🌟 *Слово дня* — красивое слово с историей\n🎲 *Случайное слово* — расширяй словарь\n📖 *Словарь* — все слова с примерами\n\n👇 *Нажми на кнопку ниже*");
                message.setReplyMarkup(createKeyboard());
            }
            else if (messageText.equals("/wordoftheday")) {
                Dictionary.WordEntry wordOfDay = Dictionary.getWordOfTheDay();
                message.setText(wordOfDay.formatWordOfTheDay());
                message.setReplyMarkup(createKeyboard());
            }
            else if (messageText.equals("/stats")) {
                String stats = db.getUserStats(userId);
                message.setText(stats);
                message.setReplyMarkup(createKeyboard());
            }
            else if (messageText.equals("/mywords")) {
                List<Map<String, String>> words = db.getUserWords(userId);
                if (words.isEmpty()) {
                    message.setText("📭 *У тебя пока нет своих слов.*\n\nВот встроенные слова: нажми кнопку 📖 Словарь\n\nДобавь свое слово: кнопка ➕ Добавить слово");
                } else {
                    StringBuilder sb = new StringBuilder("📝 *Мои слова* (" + words.size() + " слов)\n\n");
                    int count = 0;
                    for (Map<String, String> word : words) {
                        sb.append("🔹 *").append(word.get("word")).append("* — ").append(word.get("translation")).append(" ✅").append(word.get("correct")).append(" ❌").append(word.get("wrong")).append("\n");
                        count++;
                        if (count >= 15) {
                            sb.append("\n... и еще ").append(words.size() - 15).append(" слов");
                            break;
                        }
                    }
                    message.setText(sb.toString());
                }
                message.setReplyMarkup(createKeyboard());
            }
            else if (messageText.equals("/words")) {
                StringBuilder sb = new StringBuilder("📚 *Словарь* (" + Dictionary.size() + " слов)\n\n");
                int count = 0;
                for (Dictionary.WordEntry entry : Dictionary.getAllWords().values()) {
                    sb.append(entry.formatShort()).append("\n");
                    count++;
                    if (count >= 15) {
                        sb.append("\n... и еще ").append(Dictionary.size() - 15).append(" слов\nИспользуй 🎲 Случайное слово");
                        break;
                    }
                }
                message.setText(sb.toString());
                message.setReplyMarkup(createKeyboard());
            }
            else if (messageText.equals("/random")) {
                Dictionary.WordEntry randomWord = Dictionary.getRandomWord();
                message.setText(randomWord.formatFull());
                message.setReplyMarkup(createKeyboard());
            }
            else if (messageText.startsWith("/add")) {
                String[] parts = messageText.substring(5).split(" - ");
                if (parts.length == 2) {
                    String word = parts[0].trim();
                    String translation = parts[1].trim();
                    if (db.addWord(userId, word, translation)) {
                        message.setText("✅ *Слово добавлено!*\n\n" + word + " — " + translation + "\n\nПродолжай пополнять словарь! 📚\nПосмотреть все слова: кнопка 📝 Мои слова");
                    } else {
                        message.setText("❌ Не удалось добавить слово. Попробуй еще раз.");
                    }
                } else {
                    message.setText("❌ *Неправильный формат!*\n\nИспользуй: `/add слово - перевод`\nПример: `/add cat - кошка`\n\nИли нажми кнопку ➕ Добавить слово");
                }
                message.setReplyMarkup(createKeyboard());
            }

            // --- НОВАЯ ЛОГИКА ТРЕНИРОВКИ ---
            else if (messageText.equals("/train")) {
                Map<String, String> word = db.getRandomWord(userId);
                if (word != null) {
                    String englishWord = word.get("word");
                    String correctRus = word.get("translation");

                    // Бот берет 3 неправильных перевода для кнопок (убедись, что этот метод есть в Database.java!)
                    List<String> wrongs = db.getWrongTranslations(correctRus, 3);

                    message.setText("📖 *Как переводится слово:*\n\n🔤 *" + englishWord + "*");
                    // Подключаем наши кнопки
                    message.setReplyMarkup(createInlineQuizKeyboard(correctRus, wrongs));

                    // Убираем из старой сессии, если он там был, так как теперь есть кнопки
                    trainingSessions.remove(userId);
                } else {
                    message.setText("📭 *У тебя пока нет слов для тренировки!*\n\nВот встроенные слова: кнопка 📖 Словарь\nДобавь свое слово: кнопка ➕ Добавить слово");
                    message.setReplyMarkup(createKeyboard());
                }
            }
            else if (!isNewUser && !messageText.equals("/start") && !trainingSessions.containsKey(userId)) {
                message.setText("👇 *Нажми на кнопку*");
                message.setReplyMarkup(createKeyboard());
            }

            try {
                execute(message);
            } catch (TelegramApiException e) {
                System.out.println("❌ Ошибка отправки: " + e.getMessage());
            }
        }
    }

    // --- НОВЫЙ МЕТОД ДЛЯ СОЗДАНИЯ КНОПОК ВИКТОРИНЫ ---
    private InlineKeyboardMarkup createInlineQuizKeyboard(String correct, List<String> wrongs) {
        List<String> options = new ArrayList<>(wrongs);
        options.add(correct);
        Collections.shuffle(options);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (String opt : options) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(opt);
            btn.setCallbackData(opt.equals(correct) ? "quiz_correct" : "quiz_wrong");
            rows.add(Collections.singletonList(btn));
        }
        markup.setKeyboard(rows);
        return markup;
    }

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new Main());
            System.out.println("🤖 Бот успешно запущен!");
            System.out.println("📱 Найди своего бота в Telegram и нажми /start");
            System.out.println("\n📝 Кнопки: 🌟 Слово дня | 🎲 Случайное слово | 📖 Словарь | 📝 Мои слова | ➕ Добавить слово | 🎯 Тренировка | 📊 Статистика");
        } catch (TelegramApiException e) {
            System.out.println("❌ Ошибка при запуске: " + e.getMessage());
            e.printStackTrace();
        }
    }
}