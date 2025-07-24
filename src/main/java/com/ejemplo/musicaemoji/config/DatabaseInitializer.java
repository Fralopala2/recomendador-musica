package com.ejemplo.musicaemoji.config;

import com.ejemplo.musicaemoji.model.EmojiMood;
import com.ejemplo.musicaemoji.repository.EmojiMoodRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DatabaseInitializer {

    // Este @Bean se ejecutará automáticamente después de que la aplicación Spring Boot se inicie
    @Bean
    CommandLineRunner initDatabase(EmojiMoodRepository repository) {
        return args -> {
            // Verifica si la tabla emoji_mood ya tiene datos
            if (repository.count() == 0) {
                System.out.println("Cargando datos iniciales de emojis y géneros...");

                List<EmojiMood> initialData = Arrays.asList(
                    new EmojiMood(null, "😄", "Alegre", "Pop"),
                    new EmojiMood(null, "🎉", "Fiesta", "Dance"),
                    new EmojiMood(null, "🥳", "Celebración", "Reggaeton"),
                    new EmojiMood(null, "🌧️", "Melancólico", "Balada"),
                    new EmojiMood(null, "😢", "Triste", "Blues"),
                    new EmojiMood(null, "💔", "Desamor", "Indie"),
                    new EmojiMood(null, "💪", "Enérgico", "Rock"),
                    new EmojiMood(null, "⚡", "Intenso", "Electrónica"), // Original para Electrónica
                    new EmojiMood(null, "🔥", "Motivador", "Hip Hop"),
                    new EmojiMood(null, "🧘‍♀️", "Relajado", "Jazz"),
                    new EmojiMood(null, "😎", "Confiado", "R&B"),
                    new EmojiMood(null, "🤔", "Pensativo", "Clásica"),
                    new EmojiMood(null, "😴", "Tranquilo", "Ambient"),
                    new EmojiMood(null, "🤩", "Emocionado", "K-Pop"),
                    new EmojiMood(null, "🎸", "Rebelde", "Heavy Metal"),

                    // Nuevos géneros y emojis (corregidos para unicidad)
                    new EmojiMood(null, "🤠", "Campestre", "Country"),
                    new EmojiMood(null, "🌲", "Natural", "Folk"),
                    new EmojiMood(null, "🎷", "Sofisticado", "Smooth Jazz"),
                    new EmojiMood(null, "🎺", "Marcha", "Big Band"),
                    new EmojiMood(null, "🎤", "Vocal", "Soul"),
                    new EmojiMood(null, "🕺", "Groovy", "Funk"),
                    new EmojiMood(null, "💿", "Retro", "Disco"),
                    new EmojiMood(null, "💥", "Agresivo", "Punk"),
                    new EmojiMood(null, "🌫️", "Distorsionado", "Grunge"),
                    new EmojiMood(null, "⛓️", "Pesado", "Metalcore"),
                    new EmojiMood(null, "💀", "Extremo", "Death Metal"),
                    new EmojiMood(null, "🌑", "Oscuro", "Black Metal"),
                    new EmojiMood(null, "🏰", "Épico", "Symphonic Metal"),
                    new EmojiMood(null, "🌀", "Complejo", "Progressive Rock"),
                    new EmojiMood(null, "🌈", "Alucinante", "Psychedelic Rock"),
                    new EmojiMood(null, "☕", "Acogedor", "Acoustic"),
                    new EmojiMood(null, "🎧", "Relajante", "Lo-fi"),
                    new EmojiMood(null, "🌊", "Ondulante", "Chillwave"),
                    new EmojiMood(null, "�", "Nostálgico", "Synthwave"),
                    new EmojiMood(null, "🌌", "Trascendente", "Trance"),
                    new EmojiMood(null, "🏠", "Ritmo", "House"),
                    new EmojiMood(null, "⚙️", "Mecánico", "Techno"),
                    new EmojiMood(null, "🤖", "Bajo Pesado", "Dubstep"),
                    new EmojiMood(null, "🥁", "Percusivo", "Drum & Bass"),
                    new EmojiMood(null, "🇯🇲", "Caribeño", "Reggae"),
                    new EmojiMood(null, "🌶️", "Picante", "Salsa"),
                    new EmojiMood(null, "💃🏽", "Apasionado", "Flamenco"),
                    new EmojiMood(null, "🙏", "Espiritual", "Gospel"),
                    new EmojiMood(null, "🎭", "Teatral", "Opera"),
                    new EmojiMood(null, "🌍", "Global", "World Music"),
                    new EmojiMood(null, "🇮🇳", "Bollywood", "Bollywood"),
                    new EmojiMood(null, "🎌", "Anime", "Anime OST"),
                    new EmojiMood(null, "🎮", "Gamer", "Video Game OST"),
                    new EmojiMood(null, "🎬", "Cinemático", "Film Score"),
                    new EmojiMood(null, "👶", "Infantil", "Childrens Music"),
                    new EmojiMood(null, "🎄", "Navideño", "Holiday Music"),
                    new EmojiMood(null, "🗣️", "Narrativo", "Spoken Word"),
                    new EmojiMood(null, "😂", "Cómico", "Comedy"),
                    new EmojiMood(null, "💸", "Moderno", "Trap"),
                    new EmojiMood(null, "🩸", "Intenso Urbano", "Drill"), // CAMBIADO de 🔪 a 🩸
                    new EmojiMood(null, "🇬🇧", "Urbano Británico", "Grime"),
                    new EmojiMood(null, "🇰🇷", "R&B Coreano", "K-R&B"),
                    new EmojiMood(null, "🇯🇵", "Rock Japonés", "J-Rock"),
                    new EmojiMood(null, "🛹", "Juvenil", "Pop Punk"),
                    new EmojiMood(null, "🖤", "Melancólico Alternativo", "Emo"),
                    new EmojiMood(null, "🪕", "Folk Punk", "Folk Punk"),
                    new EmojiMood(null, "💡", "Indie Pop", "Indie Pop"),
                    new EmojiMood(null, "☁️", "Dream Pop", "Dream Pop"),
                    new EmojiMood(null, "🏛️", "Neoclásico", "Neoclassical"),
                    new EmojiMood(null, "🎼", "Canto", "Choral"), // CAMBIADO de 🎶 a 🎼
                    new EmojiMood(null, "🧘‍♂️", "Paz", "New Age"),
                    new EmojiMood(null, "🧖‍♀️", "Relajación", "Spa Music"),
                    new EmojiMood(null, "🍃", "Sonidos Naturales", "Nature Sounds"),
                    new EmojiMood(null, "👂", "ASMR", "ASMR"),
                    new EmojiMood(null, "📚", "Educativo", "Educational Music"),
                    new EmojiMood(null, "🤪", "Novedad", "Novelty Songs"),
                    new EmojiMood(null, "✊", "Protesta", "Political Hip Hop"),
                    new EmojiMood(null, "⚔️", "Épico Metal", "Power Metal"),
                    new EmojiMood(null, "🤘", "Viking Metal", "Viking Metal"),
                    new EmojiMood(null, "🏴‍☠️", "Pirate Metal", "Pirate Metal"),
                    new EmojiMood(null, "🌳", "Pagan Metal", "Pagan Metal"),
                    new EmojiMood(null, "🔪", "Blackened Thrash Metal", "Blackened Thrash Metal"), // CAMBIADO de ⚡ a 🔪
                    new EmojiMood(null, "👊", "Slam Death Metal", "Slam Death Metal"),
                    new EmojiMood(null, "📈", "Progressive Death Metal", "Progressive Death Metal"), // CAMBIADO de 🌀 a 📈
                    new EmojiMood(null, "🎵", "Melodic Death Metal", "Melodic Death Metal"), // CAMBIADO de 🎶 a 🎵
                    new EmojiMood(null, "⚰️", "Funeral Doom Metal", "Funeral Doom Metal"),
                    new EmojiMood(null, "👻", "Atmospheric Black Metal", "Atmospheric Black Metal"), // CAMBIADO de ☁️ a 👻
                    new EmojiMood(null, "😭", "Depressive Suicidal Black Metal (DSBM)", "Depressive Suicidal Black Metal (DSBM)"),
                    new EmojiMood(null, "💫", "Post-Black Metal", "Post-Black Metal"), // CAMBIADO de 🌌 a 💫
                    new EmojiMood(null, "🔪", "Raw Black Metal", "Raw Black Metal"), // CAMBIADO de 🔪 a 🔪 (ya estaba, pero era el segundo uso)
                    new EmojiMood(null, "🌫️", "Blackgaze", "Blackgaze"),
                    new EmojiMood(null, "🌿", "Folk Black Metal", "Folk Black Metal"), // CAMBIADO de 🌲 a 🌿
                    new EmojiMood(null, "💣", "War Metal", "War Metal"),
                    new EmojiMood(null, "🥊", "Powerviolence", "Powerviolence"), // CAMBIADO de 👊 a 🥊
                    new EmojiMood(null, "💥", "No Wave", "No Wave"),
                    new EmojiMood(null, "🎨", "Free Improvisation", "Free Improvisation"),
                    new EmojiMood(null, "🔬", "Experimental Rock", "Experimental Rock"), // CAMBIADO de 🧪 a 🔬
                    new EmojiMood(null, "😈", "Avant-garde Metal", "Avant-garde Metal"), // CAMBIADO de 🤘 a 😈
                    new EmojiMood(null, "🔊", "Drone Metal", "Drone Metal"), // CAMBIADO de 嗡 a 🔊
                    new EmojiMood(null, "🪨", "Sludgecore", "Sludgecore"), // CAMBIADO de 🧪 a 🪨
                    new EmojiMood(null, "🌃", "Post-Metal", "Post-Metal"), // CAMBIADO de 🌌 a 🌃
                    new EmojiMood(null, "🚬", "Stoner Doom", "Stoner Doom"), // CAMBIADO de 🌿 a 🚬
                    new EmojiMood(null, "🍄", "Psychedelic Doom", "Psychedelic Doom"),
                    new EmojiMood(null, "🕯️", "Traditional Doom Metal", "Traditional Doom Metal"),
                    new EmojiMood(null, "🛡️", "Epic Doom Metal", "Epic Doom Metal"),
                    new EmojiMood(null, "🍂", "Folk Doom Metal", "Folk Doom Metal"),
                    new EmojiMood(null, "⏳", "Progressive Doom Metal", "Progressive Doom Metal"),
                    new EmojiMood(null, "🛢️", "Sludge Doom Metal", "Sludge Doom Metal"),
                    new EmojiMood(null, "🏭", "Industrial", "Industrial"),
                    new EmojiMood(null, "📼", "Vaporwave", "Vaporwave"),
                    new EmojiMood(null, "🧙‍♀️", "Witch House", "Witch House"),
                    new EmojiMood(null, "👾", "Chiptune", "Chiptune"),
                    new EmojiMood(null, "📺", "TV Themes", "Television Music"),
                    new EmojiMood(null, "⚪", "Ruido Blanco", "White Noise"),
                    new EmojiMood(null, "🟫", "Ruido Marrón", "Brown Noise"),
                    new EmojiMood(null, "🩷", "Ruido Rosa", "Pink Noise"),
                    new EmojiMood(null, "🔪", "Drill Rap", "Drill Rap"),
                    new EmojiMood(null, "🧠", "Conscious Hip Hop", "Conscious Hip Hop"),
                    new EmojiMood(null, "✝️", "Christian Hip Hop", "Christian Hip Hop"),
                    new EmojiMood(null, "🤓", "Nerdcore", "Nerdcore"),
                    new EmojiMood(null, "🚗", "G-Funk", "G-Funk"),
                    new EmojiMood(null, "🔫", "Gangsta Rap", "Gangsta Rap"),
                    new EmojiMood(null, "🤬", "Grindcore", "Grindcore"),
                    new EmojiMood(null, "🪓", "Deathcore", "Deathcore"),
                    new EmojiMood(null, "🔩", "Industrial Metal", "Industrial Metal"),
                    new EmojiMood(null, "🎤", "Rap Rock", "Rap Rock"),
                    new EmojiMood(null, "🎺", "Ska Punk", "Ska Punk"),
                    new EmojiMood(null, "😭", "Emo Pop", "Emo Pop"),
                    new EmojiMood(null, "😱", "Screamo", "Screamo"),
                    new EmojiMood(null, "🏟️", "Arena Rock", "Arena Rock"),
                    new EmojiMood(null, "🎸", "Blues Rock", "Blues Rock"),
                    new EmojiMood(null, "🌾", "Folk Rock", "Folk Rock"),
                    new EmojiMood(null, "💫", "Dream Pop", "Dream Pop"),
                    new EmojiMood(null, "🧪", "Experimental Pop", "Experimental Pop"),
                    new EmojiMood(null, "🔔", "Jangle Pop", "Jangle Pop"),
                    new EmojiMood(null, "🐦", "Twee Pop", "Twee Pop"),
                    new EmojiMood(null, "👑", "Baroque Pop", "Baroque Pop"),
                    new EmojiMood(null, "🎩", "Sophisti-Pop", "Sophisti-Pop"),
                    new EmojiMood(null, "🍭", "Bubblegum Pop", "Bubblegum Pop"),
                    new EmojiMood(null, "👧", "Teen Pop", "Teen Pop"),
                    new EmojiMood(null, "👯", "Electro Swing", "Electro Swing"),
                    new EmojiMood(null, "🔮", "Future Jazz", "Future Jazz"),
                    new EmojiMood(null, "🗣️", "Vocal Jazz", "Vocal Jazz"),
                    new EmojiMood(null, "💡", "Nu Jazz", "Nu Jazz"),
                    new EmojiMood(null, "🚬", "Trip Hop", "Trip Hop"),
                    new EmojiMood(null, "☕", "Downtempo", "Downtempo"),
                    new EmojiMood(null, "✨", "Nu Disco", "Nu Disco"),
                    new EmojiMood(null, "🏡", "Tech House", "Tech House"),
                    new EmojiMood(null, "⚫", "Minimal Techno", "Minimal Techno"),
                    new EmojiMood(null, "💥", "Hardcore Techno", "Hardcore Techno"),
                    new EmojiMood(null, "🚀", "Speedcore", "Speedcore"),
                    new EmojiMood(null, "💻", "Digital Hardcore", "Digital Hardcore"),
                    new EmojiMood(null, "🕹️", "Bitpop", "Bitpop"),
                    new EmojiMood(null, "🎵", "Nintendocore", "Nintendocore"),
                    new EmojiMood(null, "🎞️", "Film Score", "Film Score"),
                    new EmojiMood(null, "🎭", "Musical Theatre", "Musical Theatre"),
                    new EmojiMood(null, "⛪", "Choral", "Choral"),
                    new EmojiMood(null, "🎶", "A Cappella", "A Cappella"),
                    new EmojiMood(null, "💈", "Barbershop", "Barbershop"),
                    new EmojiMood(null, "🌾", "Folk Pop", "Folk Pop"),
                    new EmojiMood(null, "🍂", "Indie Folk", "Indie Folk"),
                    new EmojiMood(null, "🇺🇸", "Americana", "Americana"),
                    new EmojiMood(null, "🎻", "Bluegrass", "Bluegrass"),
                    new EmojiMood(null, "🐊", "Cajun", "Cajun"),
                    new EmojiMood(null, "🌶️", "Zydeco", "Zydeco"),
                    new EmojiMood(null, "fusion", "Reggae Fusion", "Reggae Fusion"),
                    new EmojiMood(null, "🎙️", "Dancehall", "Dancehall"),
                    new EmojiMood(null, "🇹🇹", "Calypso", "Calypso"),
                    new EmojiMood(null, "🇸🇪", "Nordic Folk", "Nordic Folk"),
                    new EmojiMood(null, "🌍", "African Pop", "African Pop"),
                    new EmojiMood(null, "🇿🇦", "Kwaito", "Kwaito"),
                    new EmojiMood(null, "🔊", "Gqom", "Gqom"),
                    new EmojiMood(null, "🇲🇾", "M-Pop", "M-Pop"),
                    new EmojiMood(null, "🇹🇭", "Thai Pop", "Thai Pop"),
                    new EmojiMood(null, "🇮🇩", "Indonesian Pop", "Indonesian Pop"),
                    new EmojiMood(null, "🇵🇭", "Filipino Pop", "Filipino Pop"),
                    new EmojiMood(null, "🇹🇷", "Turkish Pop", "Turkish Pop"),
                    new EmojiMood(null, "🇸🇦", "Arabic Pop", "Arabic Pop"),
                    new EmojiMood(null, "🇮🇱", "Israeli Pop", "Israeli Pop"),
                    new EmojiMood(null, "🇷🇺", "Russian Pop", "Russian Pop"),
                    new EmojiMood(null, "🇺🇦", "Ukrainian Pop", "Ukrainian Pop"),
                    new EmojiMood(null, "🇧🇬", "Balkan Pop", "Balkan Pop"),
                    new EmojiMood(null, "🇬🇷", "Greek Pop", "Greek Pop"),
                    new EmojiMood(null, "🇪🇸", "Rock en Español", "Rock en Español"),
                    new EmojiMood(null, "🥳", "Cumbia Villera", "Cumbia Villera"),
                    new EmojiMood(null, "🇲🇽", "Norteño", "Norteño"),
                    new EmojiMood(null, "🤠", "Banda", "Banda"),
                    new EmojiMood(null, "🎵", "Ranchera", "Ranchera"),
                    new EmojiMood(null, "🇺🇸", "Tejano", "Tejano"),
                    new EmojiMood(null, "🇧🇷", "Sertanejo", "Sertanejo"),
                    new EmojiMood(null, "💃", "Forró", "Forró"),
                    new EmojiMood(null, "🥁", "Pagode", "Pagode"),
                    new EmojiMood(null, "🎊", "Axé", "Axé"),
                    new EmojiMood(null, "🎷", "Frevo", "Frevo"),
                    new EmojiMood(null, "🎼", "Choro", "Choro"),
                    new EmojiMood(null, "🎙️", "MPB", "MPB"),
                    new EmojiMood(null, "☮️", "Meditation Music", "Meditation Music"),
                    new EmojiMood(null, "🕉️", "Yoga Music", "Yoga Music"),
                    new EmojiMood(null, "📖", "Storytelling Music", "Storytelling Music"),
                    new EmojiMood(null, "😂", "Comedy Rock", "Comedy Rock"),
                    new EmojiMood(null, "🤣", "Parody Music", "Parody Music"),
                    new EmojiMood(null, "🧠", "Conscious Rap", "Conscious Rap"),
                    new EmojiMood(null, "👻", "Horrorcore", "Horrorcore"),
                    new EmojiMood(null, "🤓", "Geek Rock", "Geek Rock"),
                    new EmojiMood(null, "👽", "Filk Music", "Filk Music"),
                    new EmojiMood(null, "🛡️", "Power Metal", "Power Metal"),
                    new EmojiMood(null, "🐺", "Viking Metal", "Viking Metal"),
                    new EmojiMood(null, "☠️", "Pirate Metal", "Pirate Metal"),
                    new EmojiMood(null, "🛡️", "Medieval Metal", "Medieval Metal"),
                    new EmojiMood(null, "🌿", "Pagan Metal", "Pagan Metal"),
                    new EmojiMood(null, "🔪", "Blackened Thrash Metal", "Blackened Thrash Metal"),
                    new EmojiMood(null, "🛠️", "Technical Death Metal", "Technical Death Metal"),
                    new EmojiMood(null, "🩸", "Brutal Death Metal", "Brutal Death Metal"),
                    new EmojiMood(null, "👊", "Slam Death Metal", "Slam Death Metal"),
                    new EmojiMood(null, "🌀", "Progressive Death Metal", "Progressive Death Metal"),
                    new EmojiMood(null, "🤘", "Melodic Death Metal", "Melodic Death Metal"),
                    new EmojiMood(null, "⚰️", "Funeral Doom Metal", "Funeral Doom Metal"),
                    new EmojiMood(null, "☁️", "Atmospheric Black Metal", "Atmospheric Black Metal"),
                    new EmojiMood(null, "😭", "Depressive Suicidal Black Metal (DSBM)", "Depressive Suicidal Black Metal (DSBM)"),
                    new EmojiMood(null, "🌌", "Post-Black Metal", "Post-Black Metal"),
                    new EmojiMood(null, "🔪", "Raw Black Metal", "Raw Black Metal"),
                    new EmojiMood(null, "🌫️", "Blackgaze", "Blackgaze"),
                    new EmojiMood(null, "🌲", "Folk Black Metal", "Folk Black Metal"),
                    new EmojiMood(null, "💣", "War Metal", "War Metal"),
                    new EmojiMood(null, "💥", "Powerviolence", "Powerviolence"),
                    new EmojiMood(null, "🌊", "No Wave", "No Wave"),
                    new EmojiMood(null, "🎨", "Free Improvisation", "Free Improvisation"),
                    new EmojiMood(null, "🧪", "Experimental Rock", "Experimental Rock"),
                    new EmojiMood(null, "😈", "Avant-garde Metal", "Avant-garde Metal"),
                    new EmojiMood(null, "🔊", "Drone Metal", "Drone Metal"),
                    new EmojiMood(null, "🪨", "Sludgecore", "Sludgecore"),
                    new EmojiMood(null, "🌃", "Post-Metal", "Post-Metal"),
                    new EmojiMood(null, "🚬", "Stoner Doom", "Stoner Doom"),
                    new EmojiMood(null, "🍄", "Psychedelic Doom", "Psychedelic Doom"),
                    new EmojiMood(null, "🕯️", "Traditional Doom Metal", "Traditional Doom Metal"),
                    new EmojiMood(null, "⚔️", "Epic Doom Metal", "Epic Doom Metal"),
                    new EmojiMood(null, "🍂", "Folk Doom Metal", "Folk Doom Metal"),
                    new EmojiMood(null, "⏳", "Progressive Doom Metal", "Progressive Doom Metal"),
                    new EmojiMood(null, "🛢️", "Sludge Doom Metal", "Sludge Doom Metal")
                );

                repository.saveAll(initialData);
                System.out.println("Datos iniciales de emojis y géneros cargados.");
            } else {
                System.out.println("La tabla emoji_mood ya contiene datos. No se cargarán datos iniciales.");
            }

            // También puedes insertar usuarios aquí si lo deseas, o eliminarlos si ya están en la DB
            // Si la tabla app_user no existe o está vacía, puedes añadir lógica similar aquí.
            // Por ejemplo:
            // if (userRepository.count() == 0) {
            //     userRepository.save(new AppUser(null, "paco", "paco@example.com"));
            //     userRepository.save(new AppUser(null, "admin", "admin@example.com"));
            // }
        };
    }
}