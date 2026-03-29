package com.englishbot;

import java.util.*;

public class Dictionary {

    // Класс для хранения полной информации о слове
    public static class WordEntry {
        private String word;
        private String translation;
        private String example;
        private String meaning;
        private String origin;
        private String funFact;
        private String synonyms;
        private String antonyms;
        private String wordType;

        // Полный конструктор
        public WordEntry(String word, String translation, String wordType,
                         String meaning, String example, String origin,
                         String funFact, String synonyms, String antonyms) {
            this.word = word;
            this.translation = translation;
            this.wordType = wordType;
            this.meaning = meaning;
            this.example = example;
            this.origin = origin;
            this.funFact = funFact;
            this.synonyms = synonyms;
            this.antonyms = antonyms;
        }

        // Упрощенный конструктор
        public WordEntry(String word, String translation, String wordType,
                         String meaning, String example) {
            this(word, translation, wordType, meaning, example, "", "", "", "");
        }

        public String getWord() { return word; }
        public String getTranslation() { return translation; }
        public String getWordType() { return wordType; }
        public String getMeaning() { return meaning; }
        public String getExample() { return example; }
        public String getOrigin() { return origin; }
        public String getFunFact() { return funFact; }
        public String getSynonyms() { return synonyms; }
        public String getAntonyms() { return antonyms; }

        // Полный формат для показа
        public String formatFull() {
            StringBuilder sb = new StringBuilder();

            // Заголовок
            sb.append("📖 *").append(word.toUpperCase()).append("*\n");
            sb.append("└─ ").append(translation).append("\n\n");

            // Тип слова
            if (!wordType.isEmpty()) {
                sb.append("📌 *").append(wordType).append("*\n\n");
            }

            // Значение
            sb.append("🔹 *Значение:*\n");
            sb.append("   ").append(meaning).append("\n\n");

            // Пример
            sb.append("💬 *Пример:*\n");
            sb.append("   \"").append(example).append("\"\n\n");

            // Происхождение
            if (!origin.isEmpty()) {
                sb.append("📜 *Происхождение:*\n");
                sb.append("   ").append(origin).append("\n\n");
            }

            // Интересный факт
            if (!funFact.isEmpty()) {
                sb.append("⭐ *Интересный факт:*\n");
                sb.append("   ").append(funFact).append("\n\n");
            }

            // Синонимы и антонимы
            if (!synonyms.isEmpty() || !antonyms.isEmpty()) {
                if (!synonyms.isEmpty()) {
                    sb.append("🔄 *Синонимы:* ").append(synonyms).append("\n");
                }
                if (!antonyms.isEmpty()) {
                    sb.append("⚡ *Антонимы:* ").append(antonyms).append("\n");
                }
                sb.append("\n");
            }

            return sb.toString();
        }

        // Формат для слова дня
        public String formatWordOfTheDay() {
            StringBuilder sb = new StringBuilder();

            sb.append("🌟 *WORD OF THE DAY* 🌟\n");
            sb.append("═══════════════════\n\n");

            sb.append("📖 *").append(word.toUpperCase()).append("*\n");
            sb.append("   ").append(translation).append("\n\n");

            if (!wordType.isEmpty()) {
                sb.append("📌 *").append(wordType).append("*\n\n");
            }

            sb.append("🔹 ").append(meaning).append("\n\n");

            sb.append("💬 \"").append(example).append("\"\n\n");

            if (!origin.isEmpty()) {
                sb.append("📜 *Origin:* ").append(origin).append("\n\n");
            }

            if (!funFact.isEmpty()) {
                sb.append("✨ *Did you know?* ").append(funFact).append("\n\n");
            }

            sb.append("---\n");
            sb.append("🎯 *Want to expand your vocabulary?*\n");
            sb.append("Нажми 🎲 Случайное слово для новой порции!");

            return sb.toString();
        }

        // Краткий формат для списка
        public String formatShort() {
            return "🔹 *" + word + "* — " + translation;
        }
    }

    // Словарь с расширенной информацией
    private static final Map<String, WordEntry> DICTIONARY = new LinkedHashMap<>();

    static {
        // ========== СЛОВА ДЛЯ ИЗУЧЕНИЯ ==========

        DICTIONARY.put("serendipity", new WordEntry(
                "serendipity", "счастливая случайность", "noun",
                "The occurrence of events by chance in a happy or beneficial way.",
                "Finding that book in the used bookstore was pure serendipity.",
                "Coined by Horace Walpole in 1754, based on the Persian fairy tale 'The Three Princes of Serendip'",
                "The word was voted one of the ten English words hardest to translate in 2004!",
                "chance, luck, fortune, fluke",
                "misfortune, bad luck"
        ));

        DICTIONARY.put("ephemeral", new WordEntry(
                "ephemeral", "эфемерный, недолговечный", "adjective",
                "Lasting for a very short time; transient.",
                "The beauty of the cherry blossoms is ephemeral, lasting only a week.",
                "From Greek 'ephēmeros' meaning 'lasting only a day'",
                "Mayflies are called ephemeral because they live for just 24 hours!",
                "temporary, fleeting, short-lived, momentary",
                "permanent, eternal, everlasting"
        ));

        DICTIONARY.put("eloquent", new WordEntry(
                "eloquent", "красноречивый", "adjective",
                "Fluent or persuasive in speaking or writing.",
                "The president gave an eloquent speech that moved everyone.",
                "From Latin 'eloquentia' from 'eloqui' meaning 'to speak out'",
                "The word shares roots with 'loquacious' (talkative) and 'soliloquy' (a speech to oneself)!",
                "articulate, persuasive, expressive, fluent",
                "inarticulate, tongue-tied"
        ));

        DICTIONARY.put("nostalgia", new WordEntry(
                "nostalgia", "ностальгия", "noun",
                "A sentimental longing for the past.",
                "Listening to old songs fills me with nostalgia.",
                "From Greek 'nostos' (return home) + 'algos' (pain)",
                "Originally a medical term meaning 'homesickness', it was once considered a disease!",
                "reminiscence, sentimentality, longing",
                "indifference, forgetfulness"
        ));

        DICTIONARY.put("resilience", new WordEntry(
                "resilience", "устойчивость, жизнестойкость", "noun",
                "The capacity to recover quickly from difficulties.",
                "Her resilience helped her overcome every challenge.",
                "From Latin 'resilire' meaning 'to rebound, recoil'",
                "Resilience is like a muscle – it gets stronger the more you use it!",
                "toughness, flexibility, endurance, strength",
                "fragility, weakness"
        ));

        DICTIONARY.put("wanderlust", new WordEntry(
                "wanderlust", "страсть к путешествиям", "noun",
                "A strong desire to travel and explore the world.",
                "His wanderlust took him to over 50 countries.",
                "From German 'wandern' (to hike) + 'Lust' (desire)",
                "This word has no direct translation in many languages!",
                "adventurousness, restlessness, exploration",
                "homebody, settledness"
        ));

        DICTIONARY.put("mellifluous", new WordEntry(
                "mellifluous", "мелодичный, сладкозвучный", "adjective",
                "Sweet or musical; pleasant to hear.",
                "She had a mellifluous voice that captivated everyone.",
                "From Latin 'mel' (honey) + 'fluere' (to flow)",
                "Literally means 'flowing with honey'!",
                "melodious, harmonious, dulcet, sweet",
                "harsh, discordant, grating"
        ));

        DICTIONARY.put("petrichor", new WordEntry(
                "petrichor", "запах дождя", "noun",
                "The pleasant smell that accompanies the first rain after a dry spell.",
                "I love the petrichor after a summer thunderstorm.",
                "From Greek 'petra' (stone) + 'ichor' (the fluid that flows in the veins of gods)",
                "Scientists coined this term in 1964 – before that, there was no word for this smell!",
                "rain scent, earth smell",
                ""
        ));

        DICTIONARY.put("effervescent", new WordEntry(
                "effervescent", "игривый, жизнерадостный", "adjective",
                "Vivacious and enthusiastic; bubbly.",
                "Her effervescent personality lights up the room.",
                "From Latin 'ex' (out) + 'fervere' (to boil)",
                "Also describes carbonated drinks that fizz!",
                "bubbly, lively, vivacious, sparkling",
                "flat, dull, lifeless"
        ));

        DICTIONARY.put("biryani", new WordEntry(
                "biryani", "бирьяни (блюдо)", "noun",
                "A flavorful South Asian rice dish made with spices, meat, fish, eggs, or vegetables.",
                "The biryani was layered with fragrant basmati rice, tender chicken, and saffron.",
                "From Persian 'birian' meaning 'to fry' or 'birini' meaning 'rice'",
                "There are over 50 different types of biryani across India, Pakistan, and the Middle East!",
                "pilaf, pulao, rice dish",
                ""
        ));

        DICTIONARY.put("melancholy", new WordEntry(
                "melancholy", "меланхолия, грусть", "noun",
                "A feeling of pensive sadness, typically with no obvious cause.",
                "A sense of melancholy settled over him on rainy days.",
                "From Greek 'melan' (black) + 'chole' (bile) — ancient belief that black bile caused sadness",
                "In medieval times, melancholy was considered one of the four 'humors' that determined personality!",
                "sadness, gloom, sorrow, wistfulness",
                "happiness, joy, elation"
        ));

        DICTIONARY.put("quintessential", new WordEntry(
                "quintessential", "квинтэссенция, самый典型ный", "adjective",
                "Representing the most perfect or typical example of a quality or class.",
                "Paris is the quintessential romantic city.",
                "From Latin 'quint essentia' meaning 'fifth essence', believed to be the purest form of matter",
                "Ancient philosophers believed the universe was made of four elements + a fifth 'quintessence'!",
                "typical, classic, ideal, perfect",
                "atypical, uncharacteristic"
        ));

        DICTIONARY.put("ubiquitous", new WordEntry(
                "ubiquitous", "вездесущий", "adjective",
                "Present, appearing, or found everywhere.",
                "Smartphones have become ubiquitous in modern society.",
                "From Latin 'ubique' meaning 'everywhere'",
                "The word 'ubiquitous' itself is everywhere in tech marketing!",
                "omnipresent, universal, pervasive, everywhere",
                "rare, scarce, uncommon"
        ));

        DICTIONARY.put("zenith", new WordEntry(
                "zenith", "зенит, расцвет", "noun",
                "The time at which something is most powerful or successful.",
                "The Roman Empire reached its zenith in the 2nd century AD.",
                "From Arabic 'samt' meaning 'way' or 'path'",
                "The opposite of zenith is 'nadir', also from Arabic!",
                "peak, summit, apex, pinnacle, climax",
                "nadir, bottom, lowest point"
        ));
    }

    // Получить все слова
    public static Map<String, WordEntry> getAllWords() {
        return new LinkedHashMap<>(DICTIONARY);
    }

    // Получить случайное слово
    public static WordEntry getRandomWord() {
        List<WordEntry> entries = new ArrayList<>(DICTIONARY.values());
        Random random = new Random();
        return entries.get(random.nextInt(entries.size()));
    }

    // Получить слово дня (на основе даты)
    public static WordEntry getWordOfTheDay() {
        List<WordEntry> entries = new ArrayList<>(DICTIONARY.values());
        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        return entries.get(dayOfYear % entries.size());
    }

    // Проверить, есть ли слово в словаре
    public static boolean contains(String word) {
        return DICTIONARY.containsKey(word.toLowerCase());
    }

    // Получить слово по запросу
    public static WordEntry getWord(String word) {
        return DICTIONARY.get(word.toLowerCase());
    }

    // Поиск по слову или переводу
    public static List<WordEntry> search(String keyword) {
        List<WordEntry> results = new ArrayList<>();
        keyword = keyword.toLowerCase();
        for (WordEntry entry : DICTIONARY.values()) {
            if (entry.getWord().toLowerCase().contains(keyword) ||
                    entry.getTranslation().toLowerCase().contains(keyword)) {
                results.add(entry);
            }
        }
        return results;
    }

    // Количество слов в словаре
    public static int size() {
        return DICTIONARY.size();
    }
}