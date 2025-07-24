package com.ejemplo.musicaemoji.service;

import com.ejemplo.musicaemoji.model.EmojiMood;
import com.ejemplo.musicaemoji.repository.EmojiMoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final EmojiMoodRepository emojiMoodRepository;

    @Autowired
    public RecommendationService(EmojiMoodRepository emojiMoodRepository) {
        this.emojiMoodRepository = emojiMoodRepository;
    }

    @Transactional(readOnly = true)
    public List<EmojiMood> getAllEmojiMoods() {
        return emojiMoodRepository.findAll();
    }

    @Transactional
    public EmojiMood createEmojiMood(EmojiMood emojiMood) {
        return emojiMoodRepository.save(emojiMood);
    }

    @Transactional
    public EmojiMood updateEmojiMood(Long id, EmojiMood updatedEmojiMood) {
        return emojiMoodRepository.findById(id).map(emojiMood -> {
            emojiMood.setEmoji(updatedEmojiMood.getEmoji());
            emojiMood.setMoodDescription(updatedEmojiMood.getMoodDescription());
            emojiMood.setGenreHint(updatedEmojiMood.getGenreHint());
            return emojiMoodRepository.save(emojiMood);
        }).orElseThrow(() -> new RuntimeException("EmojiMood not found with id " + id));
    }

    @Transactional
    public void deleteEmojiMood(Long id) {
        emojiMoodRepository.deleteById(id);
    }

    public Set<String> recommendGenresByEmojis(String emojisInput) {
        Set<String> recommendedGenres = new HashSet<>();
        boolean directMatchFound = false;

        emojisInput.codePoints().forEach(codePoint -> {
            String emoji = new String(Character.toChars(codePoint));
            emojiMoodRepository.findByEmoji(emoji).ifPresent(mood -> {
                recommendedGenres.add(mood.getGenreHint());
            });
        });

        if (!recommendedGenres.isEmpty()) {
            directMatchFound = true;
        }

        if (!directMatchFound) {
            emojisInput.codePoints().forEach(codePoint -> {
                String emoji = new String(Character.toChars(codePoint));
                if ("🤷‍♀️".equals(emoji)) {
                    recommendedGenres.add("Indie");
                } else if ("🎉".equals(emoji)) {
                    recommendedGenres.add("Pop");
                    recommendedGenres.add("Dance");
                }
            });
        }

        if (recommendedGenres.isEmpty() && !emojisInput.isEmpty()) {
            recommendedGenres.add("Indie");
        }

        return recommendedGenres.stream()
                                .filter(genre -> !genre.isEmpty())
                                .collect(Collectors.toSet());
    }

    public List<String> getSampleSongsForGenres(Set<String> genres) {
        List<String> songs = new ArrayList<>();
        Map<String, List<String>> genreSamples = new HashMap<>();

        // Géneros existentes
        genreSamples.put("Pop", Arrays.asList(
            "Blinding Lights - The Weeknd (Pop)",
            "Shape of You - Ed Sheeran (Pop)",
            "Uptown Funk - Mark Ronson ft. Bruno Mars (Pop)",
            "Bad Guy - Billie Eilish (Pop)",
            "Despacito - Luis Fonsi ft. Daddy Yankee (Pop)",
            "Dance Monkey - Tones and I (Pop)",
            "Rolling in the Deep - Adele (Pop)",
            "Happy - Pharrell Williams (Pop)",
            "Old Town Road - Lil Nas X ft. Billy Ray Cyrus (Pop)",
            "Levitating - Dua Lipa (Pop)"
        ));

        genreSamples.put("Rock", Arrays.asList(
            "Bohemian Rhapsody - Queen (Rock)",
            "Stairway to Heaven - Led Zeppelin (Rock)",
            "Smells Like Teen Spirit - Nirvana (Rock)",
            "Sweet Child o' Mine - Guns N' Roses (Rock)",
            "Hotel California - Eagles (Rock)",
            "Livin' on a Prayer - Bon Jovi (Rock)",
            "Thunderstruck - AC/DC (Rock)",
            "Seven Nation Army - The White Stripes (Rock)",
            "Wonderwall - Oasis (Rock)",
            "Dream On - Aerosmith (Rock)"
        ));

        genreSamples.put("Dance", Arrays.asList(
            "Titanium - David Guetta ft. Sia (Dance)",
            "Wake Me Up - Avicii (Dance)",
            "Levels - Avicii (Dance)",
            "Lean On - Major Lazer & DJ Snake ft. MØ (Dance)",
            "Where Are Ü Now - Skrillex & Diplo ft. Justin Bieber (Dance)",
            "One Kiss - Calvin Harris & Dua Lipa (Dance)",
            "The Nights - Avicii (Dance)",
            "Don't You Worry Child - Swedish House Mafia ft. John Martin (Dance)",
            "I Gotta Feeling - The Black Eyed Peas (Dance)",
            "Closer - The Chainsmokers ft. Halsey (Dance)"
        ));

        genreSamples.put("Blues", Arrays.asList(
            "The Thrill Is Gone - B.B. King (Blues)",
            "Cross Road Blues - Robert Johnson (Blues)",
            "Sweet Home Chicago - Robert Johnson (Blues)",
            "Mannish Boy - Muddy Waters (Blues)",
            "Boom Boom - John Lee Hooker (Blues)",
            "Pride and Joy - Stevie Ray Vaughan (Blues)",
            "Red House - Jimi Hendrix (Blues)",
            "Born Under a Bad Sign - Albert King (Blues)",
            "Hoochie Coochie Man - Muddy Waters (Blues)",
            "Stormy Monday - T-Bone Walker (Blues)"
        ));

        genreSamples.put("Hip Hop", Arrays.asList(
            "Lose Yourself - Eminem (Hip Hop)",
            "Still D.R.E. - Dr. Dre ft. Snoop Dogg (Hip Hop)",
            "N.Y. State of Mind - Nas (Hip Hop)",
            "California Love - 2Pac ft. Dr. Dre (Hip Hop)",
            "In Da Club - 50 Cent (Hip Hop)",
            "Hotline Bling - Drake (Hip Hop)",
            "God's Plan - Drake (Hip Hop)",
            "Sicko Mode - Travis Scott (Hip Hop)",
            "Mo Bamba - Sheck Wes (Hip Hop)",
            "Old Town Road - Lil Nas X (Hip Hop)"
        ));

        genreSamples.put("Indie", Arrays.asList(
            "Do I Wanna Know? - Arctic Monkeys (Indie)",
            "R U Mine? - Arctic Monkeys (Indie)",
            "Chasing Cars - Snow Patrol (Indie)",
            "Ho Hey - The Lumineers (Indie)",
            "Little Talks - Of Monsters and Men (Indie)",
            "Pumped Up Kicks - Foster the People (Indie)",
            "Sweater Weather - The Neighbourhood (Indie)",
            "Take Me Out - Franz Ferdinand (Indie)",
            "Mr. Brightside - The Killers (Indie)",
            "Somebody That I Used to Know - Gotye ft. Kimbra (Indie)"
        ));

        genreSamples.put("Reggaeton", Arrays.asList(
            "Gasolina - Daddy Yankee (Reggaeton)",
            "Dura - Daddy Yankee (Reggaeton)",
            "Con Calma - Daddy Yankee & Snow (Reggaeton)",
            "Despacito - Luis Fonsi ft. Daddy Yankee (Reggaeton)",
            "Mi Gente - J Balvin & Willy William (Reggaeton)",
            "X - Nicky Jam & J Balvin (Reggaeton)",
            "Calma - Pedro Capó & Farruko (Reggaeton)",
            "Taki Taki - DJ Snake ft. Selena Gomez, Ozuna, Cardi B (Reggaeton)",
            "China - Anuel AA, Daddy Yankee, Karol G, Ozuna, J Balvin (Reggaeton)",
            "Safaera - Bad Bunny, Jowell & Randy, Ñengo Flow (Reggaeton)"
        ));

        genreSamples.put("Balada", Arrays.asList(
            "Contigo en la distancia - Christina Aguilera (Balada)",
            "My Heart Will Go On - Celine Dion (Balada)",
            "I Will Always Love You - Whitney Houston (Balada)",
            "Unbreak My Heart - Toni Braxton (Balada)",
            "Hello - Adele (Balada)",
            "Someone Like You - Adele (Balada)",
            "All of Me - John Legend (Balada)",
            "Perfect - Ed Sheeran (Balada)",
            "Hallelujah - Leonard Cohen (Balada)",
            "Can't Help Falling in Love - Elvis Presley (Balada)"
        ));

        genreSamples.put("Electrónica", Arrays.asList(
            "Strobe - Deadmau5 (Electrónica)",
            "Levels - Avicii (Electrónica)",
            "Animals - Martin Garrix (Electrónica)",
            "Clarity - Zedd ft. Foxes (Electrónica)",
            "Faded - Alan Walker (Electrónica)",
            "Alone - Marshmello (Electrónica)",
            "The Middle - Zedd, Maren Morris, Grey (Electrónica)",
            "Lean On - Major Lazer & DJ Snake (Electrónica)",
            "Where Are Ü Now - Skrillex & Diplo ft. Justin Bieber (Electrónica)",
            "Ghosts 'n' Stuff - Deadmau5 ft. Rob Swire (Electrónica)"
        ));

        genreSamples.put("Jazz", Arrays.asList(
            "Take Five - Dave Brubeck Quartet (Jazz)",
            "So What - Miles Davis (Jazz)",
            "My Favorite Things - John Coltrane (Jazz)",
            "What a Wonderful World - Louis Armstrong (Jazz)",
            "Fly Me to the Moon - Frank Sinatra (Jazz)",
            "Summertime - Ella Fitzgerald (Jazz)",
            "Giant Steps - John Coltrane (Jazz)",
            "All of Me - Billie Holiday (Jazz)",
            "Autumn Leaves - Cannonball Adderley (Jazz)",
            "Blue in Green - Miles Davis (Jazz)"
        ));

        genreSamples.put("R&B", Arrays.asList(
            "Crazy in Love - Beyoncé ft. Jay-Z (R&B)",
            "No Scrubs - TLC (R&B)",
            "Waterfalls - TLC (R&B)",
            "Say My Name - Destiny's Child (R&B)",
            "U Remind Me - Usher (R&B)",
            "If I Ain't Got You - Alicia Keys (R&B)",
            "We Belong Together - Mariah Carey (R&B)",
            "Finesse - Bruno Mars ft. Cardi B (R&B)",
            "Leave The Door Open - Silk Sonic (R&B)",
            "Essence - Wizkid ft. Tems (R&B)"
        ));

        genreSamples.put("Clásica", Arrays.asList(
            "Sinfonía No. 5 - Ludwig van Beethoven (Clásica)",
            "Für Elise - Ludwig van Beethoven (Clásica)",
            "Eine kleine Nachtmusik - Wolfgang Amadeus Mozart (Clásica)",
            "Claro de Luna - Claude Debussy (Clásica)",
            "Las Cuatro Estaciones - Antonio Vivaldi (Clásica)",
            "Marcha Turca - Wolfgang Amadeus Mozart (Clásica)",
            "Ave Maria - Franz Schubert (Clásica)",
            "Canon en Re Mayor - Johann Pachelbel (Clásica)",
            "Concierto para piano No. 2 - Sergei Rachmaninoff (Clásica)",
            "O Fortuna (Carmina Burana) - Carl Orff (Clásica)"
        ));

        genreSamples.put("Ambient", Arrays.asList(
            "Ambient 1: Music for Airports - Brian Eno (Ambient)",
            "Weightless - Marconi Union (Ambient)",
            "Selected Ambient Works 85-92 - Aphex Twin (Ambient)",
            "An Ending (Ascent) - Brian Eno (Ambient)",
            "Reflection - Brian Eno (Ambient)",
            "Structures From Silence - Steve Roach (Ambient)",
            "The Pearl - Harold Budd & Brian Eno (Ambient)",
            "Lux - Brian Eno (Ambient)",
            "Deep Blue - Biosphere (Ambient)",
            "Microgravity - The Orb (Ambient)"
        ));

        genreSamples.put("K-Pop", Arrays.asList(
            "Dynamite - BTS (K-Pop)",
            "DDU-DU DDU-DU - BLACKPINK (K-Pop)",
            "Gangnam Style - PSY (K-Pop)",
            "God's Menu - Stray Kids (K-Pop)",
            "Psycho - Red Velvet (K-Pop)",
            "Love Shot - EXO (K-Pop)",
            "Feel Special - TWICE (K-Pop)",
            "Boy With Luv - BTS ft. Halsey (K-Pop)",
            "Kill This Love - BLACKPINK (K-Pop)",
            "Gee - Girls' Generation (K-Pop)"
        ));

        genreSamples.put("Heavy Metal", Arrays.asList(
            "Master of Puppets - Metallica (Heavy Metal)",
            "Iron Man - Black Sabbath (Heavy Metal)",
            "Paranoid - Black Sabbath (Heavy Metal)",
            "Enter Sandman - Metallica (Heavy Metal)",
            "Holy Wars... The Punishment Due - Megadeth (Heavy Metal)",
            "Hallowed Be Thy Name - Iron Maiden (Heavy Metal)",
            "Ace of Spades - Motörhead (Heavy Metal)",
            "War Pigs - Black Sabbath (Heavy Metal)",
            "Breaking the Law - Judas Priest (Heavy Metal)",
            "Run to the Hills - Iron Maiden (Heavy Metal)"
        ));

        // Nuevos géneros añadidos
        genreSamples.put("Country", Arrays.asList(
            "Take Me Home, Country Roads - John Denver (Country)",
            "Jolene - Dolly Parton (Country)",
            "Ring of Fire - Johnny Cash (Country)",
            "I Walk The Line - Johnny Cash (Country)",
            "Crazy - Patsy Cline (Country)",
            "Friends in Low Places - Garth Brooks (Country)",
            "The Dance - Garth Brooks (Country)",
            "Before He Cheats - Carrie Underwood (Country)",
            "Humble and Kind - Tim McGraw (Country)",
            "Body Like a Back Road - Sam Hunt (Country)"
        ));

        genreSamples.put("Folk", Arrays.asList(
            "Blowin' in the Wind - Bob Dylan (Folk)",
            "The Sound of Silence - Simon & Garfunkel (Folk)",
            "Hallelujah - Leonard Cohen (Folk)",
            "Suzanne - Leonard Cohen (Folk)",
            "American Pie - Don McLean (Folk)",
            "Big Yellow Taxi - Joni Mitchell (Folk)",
            "Fast Car - Tracy Chapman (Folk)",
            "Mr. Tambourine Man - Bob Dylan (Folk)",
            "Where Did You Sleep Last Night - Lead Belly (Folk)",
            "House of the Rising Sun - The Animals (Folk)"
        ));

        genreSamples.put("Smooth Jazz", Arrays.asList(
            "Morning Dance - Spyro Gyra (Smooth Jazz)",
            "Maputo - Bob James & David Sanborn (Smooth Jazz)",
            "Breezin' - George Benson (Smooth Jazz)",
            "Feels So Good - Chuck Mangione (Smooth Jazz)",
            "Just the Two of Us - Grover Washington Jr. (Smooth Jazz)",
            "The Look of Love - Diana Krall (Smooth Jazz)",
            "Midnight at the Oasis - Maria Muldaur (Smooth Jazz)",
            "Street Life - The Crusaders (Smooth Jazz)",
            "Europa (Earth's Cry Heaven's Smile) - Santana (Smooth Jazz)",
            "Winelight - Grover Washington Jr. (Smooth Jazz)"
        ));

        genreSamples.put("Big Band", Arrays.asList(
            "In the Mood - Glenn Miller (Big Band)",
            "Sing, Sing, Sing - Benny Goodman (Big Band)",
            "Take the 'A' Train - Duke Ellington (Big Band)",
            "One O'Clock Jump - Count Basie (Big Band)",
            "Moonlight Serenade - Glenn Miller (Big Band)",
            "Stompin' at the Savoy - Benny Goodman (Big Band)",
            "Lester Leaps In - Count Basie (Big Band)",
            "Caravan - Duke Ellington (Big Band)",
            "Jumpin' at the Woodside - Count Basie (Big Band)",
            "Don't Be That Way - Benny Goodman (Big Band)"
        ));

        genreSamples.put("Soul", Arrays.asList(
            "What's Going On - Marvin Gaye (Soul)",
            "Respect - Aretha Franklin (Soul)",
            "I Heard It Through the Grapevine - Marvin Gaye (Soul)",
            "Sitting on the Dock of the Bay - Otis Redding (Soul)",
            "A Change Is Gonna Come - Sam Cooke (Soul)",
            "Let's Get It On - Marvin Gaye (Soul)",
            "Superstition - Stevie Wonder (Soul)",
            "At Last - Etta James (Soul)",
            "Stand by Me - Ben E. King (Soul)",
            "(Sittin' On) The Dock of the Bay - Otis Redding (Soul)"
        ));

        genreSamples.put("Funk", Arrays.asList(
            "Super Freak - Rick James (Funk)",
            "Give Up the Funk (Tear the Roof off the Sucker) - Parliament (Funk)",
            "Flash Light - Parliament (Funk)",
            "Brick House - Commodores (Funk)",
            "Play That Funky Music - Wild Cherry (Funk)",
            "Get Up (I Feel Like Being a) Sex Machine - James Brown (Funk)",
            "Shining Star - Earth, Wind & Fire (Funk)",
            "September - Earth, Wind & Fire (Funk)",
            "Chic Cheer - Chic (Funk)",
            "Le Freak - Chic (Funk)"
        ));

        genreSamples.put("Disco", Arrays.asList(
            "Stayin' Alive - Bee Gees (Disco)",
            "I Will Survive - Gloria Gaynor (Disco)",
            "Le Freak - Chic (Disco)",
            "Boogie Wonderland - Earth, Wind & Fire (Disco)",
            "Disco Inferno - The Trammps (Disco)",
            "Y.M.C.A. - Village People (Disco)",
            "Night Fever - Bee Gees (Disco)",
            "Funkytown - Lipps Inc. (Disco)",
            "Last Dance - Donna Summer (Disco)",
            "Good Times - Chic (Disco)"
        ));

        genreSamples.put("Punk", Arrays.asList(
            "Blitzkrieg Bop - Ramones (Punk)",
            "God Save the Queen - Sex Pistols (Punk)",
            "London Calling - The Clash (Punk)",
            "Should I Stay or Should I Go - The Clash (Punk)",
            "Anarchy in the U.K. - Sex Pistols (Punk)",
            "California Über Alles - Dead Kennedys (Punk)",
            "White Riot - The Clash (Punk)",
            "I Wanna Be Sedated - Ramones (Punk)",
            "Neat Neat Neat - The Damned (Punk)",
            "Ever Fallen in Love (With Someone You Shouldn't've) - Buzzcocks (Punk)"
        ));

        genreSamples.put("Grunge", Arrays.asList(
            "Smells Like Teen Spirit - Nirvana (Grunge)",
            "Black Hole Sun - Soundgarden (Grunge)",
            "Alive - Pearl Jam (Grunge)",
            "Man in the Box - Alice in Chains (Grunge)",
            "Would? - Alice in Chains (Grunge)",
            "Come As You Are - Nirvana (Grunge)",
            "Jeremy - Pearl Jam (Grunge)",
            "Rooster - Alice in Chains (Grunge)",
            "Spoonman - Soundgarden (Grunge)",
            "Them Bones - Alice in Chains (Grunge)"
        ));

        genreSamples.put("Metalcore", Arrays.asList(
            "The End of Heartache - Killswitch Engage (Metalcore)",
            "My Curse - Killswitch Engage (Metalcore)",
            "Holy Diver - Killswitch Engage (Metalcore)",
            "Vice Grip - Parkway Drive (Metalcore)",
            "Carrion - Parkway Drive (Metalcore)",
            "Bottom Feeder - Parkway Drive (Metalcore)",
            "Confined - As I Lay Dying (Metalcore)",
            "Through Struggle - As I Lay Dying (Metalcore)",
            "The Darkest Nights - As I Lay Dying (Metalcore)",
            "A Decade in White - August Burns Red (Metalcore)"
        ));

        genreSamples.put("Death Metal", Arrays.asList(
            "Hammer Smashed Face - Cannibal Corpse (Death Metal)",
            "Pull the Plug - Death (Death Metal)",
            "Crystal Mountain - Death (Death Metal)",
            "Left Hand Path - Entombed (Death Metal)",
            "Blinded by Fear - At the Gates (Death Metal)",
            "Slaughter of the Soul - At the Gates (Death Metal)",
            "Spiritual Healing - Death (Death Metal)",
            "Leprosy - Death (Death Metal)",
            "From the Cradle to Enslave - Cradle of Filth (Death Metal)",
            "Evisceration Plague - Cannibal Corpse (Death Metal)"
        ));

        genreSamples.put("Black Metal", Arrays.asList(
            "Freezing Moon - Mayhem (Black Metal)",
            "Dunkelheit - Burzum (Black Metal)",
            "Transilvanian Hunger - Darkthrone (Black Metal)",
            "Chainsaw Gutsfuck - Mayhem (Black Metal)",
            "In the Nightside Eclipse - Emperor (Black Metal)",
            "Pure Fucking Armageddon - Mayhem (Black Metal)",
            "A Fine Day to Die - Bathory (Black Metal)",
            "Mother North - Satyricon (Black Metal)",
            "Nemesis Divina - Satyricon (Black Metal)",
            "Under a Funeral Moon - Darkthrone (Black Metal)"
        ));

        genreSamples.put("Symphonic Metal", Arrays.asList(
            "Nemo - Nightwish (Symphonic Metal)",
            "Amaranth - Nightwish (Symphonic Metal)",
            "Ghost Love Score - Nightwish (Symphonic Metal)",
            "Within Temptation - Ice Queen (Symphonic Metal)",
            "Mother Earth - Within Temptation (Symphonic Metal)",
            "Our Solemn Hour - Within Temptation (Symphonic Metal)",
            "Sanctuary - Kamelot (Symphonic Metal)",
            "Sacrimony (Angel of Afterlife) - Kamelot (Symphonic Metal)",
            "Forever - Stratovarius (Symphonic Metal)",
            "Higher Than Hope - Nightwish (Symphonic Metal)"
        ));

        genreSamples.put("Progressive Rock", Arrays.asList(
            "Stairway to Heaven - Led Zeppelin (Progressive Rock)", // También Rock
            "Comfortably Numb - Pink Floyd (Progressive Rock)",
            "Bohemian Rhapsody - Queen (Progressive Rock)", // También Pop/Rock
            "Roundabout - Yes (Progressive Rock)",
            "21st Century Schizoid Man - King Crimson (Progressive Rock)",
            "Close to the Edge - Yes (Progressive Rock)",
            "Firth of Fifth - Genesis (Progressive Rock)",
            "Money - Pink Floyd (Progressive Rock)",
            "The Court of the Crimson King - King Crimson (Progressive Rock)",
            "Watcher of the Skies - Genesis (Progressive Rock)"
        ));

        genreSamples.put("Psychedelic Rock", Arrays.asList(
            "Light My Fire - The Doors (Psychedelic Rock)",
            "Purple Haze - Jimi Hendrix (Psychedelic Rock)",
            "White Rabbit - Jefferson Airplane (Psychedelic Rock)",
            "Lucy in the Sky with Diamonds - The Beatles (Psychedelic Rock)",
            "Sunshine of Your Love - Cream (Psychedelic Rock)",
            "Spirit in the Sky - Norman Greenbaum (Psychedelic Rock)",
            "In-A-Gadda-Da-Vida - Iron Butterfly (Psychedelic Rock)",
            "Strawberry Fields Forever - The Beatles (Psychedelic Rock)",
            "Eight Miles High - The Byrds (Psychedelic Rock)",
            "Voodoo Child (Slight Return) - Jimi Hendrix (Psychedelic Rock)"
        ));

        genreSamples.put("Acoustic", Arrays.asList(
            "Wonderwall (Acoustic) - Oasis (Acoustic)",
            "Hallelujah (Acoustic) - Jeff Buckley (Acoustic)",
            "Tears in Heaven - Eric Clapton (Acoustic)",
            "Fast Car - Tracy Chapman (Acoustic)",
            "Give Me Love (Give Me Peace on Earth) - George Harrison (Acoustic)",
            "Wish You Were Here (Acoustic) - Pink Floyd (Acoustic)",
            "Here Comes The Sun (Acoustic) - The Beatles (Acoustic)",
            "Dust in the Wind - Kansas (Acoustic)",
            "More Than Words - Extreme (Acoustic)",
            "Hotel California (Acoustic) - Eagles (Acoustic)"
        ));

        genreSamples.put("Lo-fi", Arrays.asList(
            "Lo-fi Study Beats - Lofi Girl (Lo-fi)",
            "Coffee Shop Jazz - Jazz Vibes (Lo-fi)",
            "Chill Lofi Hip Hop - Chillhop Music (Lo-fi)",
            "Night Owl - Lofi Fruits Music (Lo-fi)",
            "Lazy Sunday - Lofi Girl (Lo-fi)",
            "Warmth - Lofi Fruits Music (Lo-fi)",
            "Rainy Day - Lofi Girl (Lo-fi)",
            "Autumn Leaves (Lo-fi) - Lofi Fruits Music (Lo-fi)",
            "Smooth Chill - Chillhop Music (Lo-fi)",
            "Late Night Drive - Lofi Girl (Lo-fi)"
        ));

        genreSamples.put("Chillwave", Arrays.asList(
            "Feel It All Around - Washed Out (Chillwave)",
            "Awake - Tycho (Chillwave)",
            "O.N.E. - Yeasayer (Chillwave)",
            "Dayvan Cowboy - Boards of Canada (Chillwave)",
            "I Want You - Washed Out (Chillwave)",
            "Coastal Brake - Tycho (Chillwave)",
            "Japanese Denim - Daniel Caesar (Chillwave)",
            "New Theory - Washed Out (Chillwave)",
            "A Walk - Tycho (Chillwave)",
            "All I Need - Air (Chillwave)"
        ));

        genreSamples.put("Synthwave", Arrays.asList(
            "Nightcall - Kavinsky (Synthwave)",
            "Resonance - HOME (Synthwave)",
            "The Midnight - Los Angeles (Synthwave)",
            "Turbulence - Miami Nights 1984 (Synthwave)",
            "Red Eyes - The Midnight (Synthwave)",
            "Neon Medusa - Lazerhawk (Synthwave)",
            "Roller Mobster - Carpenter Brut (Synthwave)",
            "Inner Universe - Ghost Data (Synthwave)",
            "Sunset - The Midnight (Synthwave)",
            "Lost Boy - The Midnight (Synthwave)"
        ));

        genreSamples.put("Trance", Arrays.asList(
            "Adagio for Strings - Tiësto (Trance)",
            "Communication - Armin van Buuren (Trance)",
            "Silence - Delerium ft. Sarah McLachlan (Trance)",
            "Carte Blanche - Veracocha (Trance)",
            "For An Angel - Paul van Dyk (Trance)",
            "Gouryella - Gouryella (Trance)",
            "Southern Sun - Paul Oakenfold ft. Carla Werner (Trance)",
            "As the Rush Comes - Motorcycle (Trance)",
            "In and Out of Love - Armin van Buuren ft. Sharon den Adel (Trance)",
            "Concrete Angel - Gareth Emery ft. Christina Novelli (Trance)"
        ));

        genreSamples.put("House", Arrays.asList(
            "One More Time - Daft Punk (House)",
            "Around the World - Daft Punk (House)",
            "Starlight - Supermen Lovers ft. Mani Hoffman (House)",
            "Praise You - Fatboy Slim (House)",
            "Where Love Lives - Alison Limerick (House)",
            "Show Me Love - Robin S. (House)",
            "Gypsy Woman (She's Homeless) - Crystal Waters (House)",
            "The Rhythm of the Night - Corona (House)",
            "Finally - CeCe Peniston (House)",
            "Love Can't Turn Around - Farley Jackmaster Funk (House)"
        ));

        genreSamples.put("Techno", Arrays.asList(
            "Insomnia - Faithless (Techno)",
            "Born Slippy .NUXX - Underworld (Techno)",
            "Strings of Life - Derrick May (Techno)",
            "The Bells - Jeff Mills (Techno)",
            "Knights of the Jaguar - DJ Rolando (Techno)",
            "Dominator - Human Resource (Techno)",
            "Energy Flash - Joey Beltram (Techno)",
            "Plastic Dreams - Jaydee (Techno)",
            "Acid Tracks - Phuture (Techno)",
            "Higher State of Consciousness - Josh Wink (Techno)"
        ));

        genreSamples.put("Dubstep", Arrays.asList(
            "Scary Monsters and Nice Sprites - Skrillex (Dubstep)",
            "Bangarang - Skrillex ft. Sirah (Dubstep)",
            "Internet Friends - Knife Party (Dubstep)",
            "Centipede - Knife Party (Dubstep)",
            "Crush on You - Nero (Dubstep)",
            "Promises - Nero (Dubstep)",
            "Holdin' On - Flume (Dubstep)",
            "You & Me (Flume Remix) - Disclosure ft. Eliza Doolittle (Dubstep)",
            "Gold Dust - DJ Fresh (Dubstep)",
            "I Can't Stop - Flux Pavilion (Dubstep)"
        ));

        genreSamples.put("Drum & Bass", Arrays.asList(
            "Inner City Life - Goldie (Drum & Bass)",
            "Original Nuttah - UK Apachi & Shy FX (Drum & Bass)",
            "Circles - Adam F (Drum & Bass)",
            "Brown Paper Bag - Roni Size & Reprazent (Drum & Bass)",
            "LK - DJ Marky & XRS ft. Stamina MC (Drum & Bass)",
            "Barcelona - D.Kay & Epsilon (Drum & Bass)",
            "Voodoo People (Pendulum Remix) - The Prodigy (Drum & Bass)",
            "Tarantula - Pendulum (Drum & Bass)",
            "Come with Me - DJ Fresh (Drum & Bass)",
            "Feel the Love - Rudimental ft. John Newman (Drum & Bass)"
        ));

        genreSamples.put("Reggae", Arrays.asList(
            "One Love - Bob Marley & The Wailers (Reggae)",
            "No Woman, No Cry - Bob Marley & The Wailers (Reggae)",
            "Redemption Song - Bob Marley & The Wailers (Reggae)",
            "Stir It Up - Bob Marley & The Wailers (Reggae)",
            "Three Little Birds - Bob Marley & The Wailers (Reggae)",
            "Buffalo Soldier - Bob Marley & The Wailers (Reggae)",
            "Is This Love - Bob Marley & The Wailers (Reggae)",
            "Get Up, Stand Up - Bob Marley & The Wailers (Reggae)",
            "Jamming - Bob Marley & The Wailers (Reggae)",
            "Could You Be Loved - Bob Marley & The Wailers (Reggae)"
        ));

        genreSamples.put("Salsa", Arrays.asList(
            "La Vida Es Un Carnaval - Celia Cruz (Salsa)",
            "Vivir Mi Vida - Marc Anthony (Salsa)",
            "Valió la Pena - Marc Anthony (Salsa)",
            "Idilio - Willie Colón (Salsa)",
            "El Cantante - Hector Lavoe (Salsa)",
            "Rebelión - Joe Arroyo (Salsa)",
            "Pedro Navaja - Rubén Blades (Salsa)",
            "Mi Gente - Hector Lavoe (Salsa)",
            "Aguanile - Marc Anthony (Salsa)",
            "Yo No Sé Mañana - Luis Enrique (Salsa)"
        ));

        genreSamples.put("Flamenco", Arrays.asList(
            "Entre Dos Aguas - Paco de Lucía (Flamenco)",
            "Al Verte Las Flores Lloran - Camarón de la Isla (Flamenco)",
            "Volando Voy - Kiko Veneno (Flamenco)",
            "Soy Gitano - Camarón de la Isla (Flamenco)",
            "Con la Guitarra en la Mano - Paco de Lucía (Flamenco)",
            "Como el Agua - Camarón de la Isla (Flamenco)",
            "Chanela - Paco de Lucía (Flamenco)",
            "Nana del Caballo Grande - Camarón de la Isla (Flamenco)",
            "La Leyenda del Tiempo - Camarón de la Isla (Flamenco)",
            "Zyryab - Paco de Lucía (Flamenco)"
        ));

        genreSamples.put("Gospel", Arrays.asList(
            "Amazing Grace - Aretha Franklin (Gospel)",
            "Oh Happy Day - Edwin Hawkins Singers (Gospel)",
            "Take My Hand, Precious Lord - Mahalia Jackson (Gospel)",
            "His Eye Is on the Sparrow - Mahalia Jackson (Gospel)",
            "I'll Take You There - The Staple Singers (Gospel)",
            "This Little Light of Mine - Traditional (Gospel)",
            "How Great Thou Art - Alan Jackson (Gospel)",
            "Mary, Don't You Weep - The Fisk Jubilee Singers (Gospel)",
            "Wade in the Water - Traditional (Gospel)",
            "Swing Low, Sweet Chariot - Traditional (Gospel)"
        ));

        genreSamples.put("Opera", Arrays.asList(
            "Nessun Dorma - Giacomo Puccini (Opera)",
            "Habanera (Carmen) - Georges Bizet (Opera)",
            "Queen of the Night Aria (Magic Flute) - Mozart (Opera)",
            "La donna è mobile (Rigoletto) - Giuseppe Verdi (Opera)",
            "Libiamo ne' lieti calici (La Traviata) - Giuseppe Verdi (Opera)",
            "Un bel dì vedremo (Madama Butterfly) - Giacomo Puccini (Opera)",
            "Va, pensiero (Nabucco) - Giuseppe Verdi (Opera)",
            "O mio babbino caro (Gianni Schicchi) - Giacomo Puccini (Opera)",
            "Vesti la giubba (Pagliacci) - Ruggero Leoncavallo (Opera)",
            "Largo al factotum (The Barber of Seville) - Gioachino Rossini (Opera)"
        ));

        genreSamples.put("World Music", Arrays.asList(
            "Pata Pata - Miriam Makeba (World Music)",
            "Sodade - Cesária Évora (World Music)",
            "Sultans of Swing - Dire Straits (World Music)", // Con influencias
            "El Cuarto de Tula - Buena Vista Social Club (World Music)",
            "Chan Chan - Buena Vista Social Club (World Music)",
            "Oye Como Va - Santana (World Music)",
            "Gasolina - Daddy Yankee (World Music)", // También Reggaeton
            "Waka Waka (This Time for Africa) - Shakira (World Music)",
            "Lambada - Kaoma (World Music)",
            "Volare (Nel blu dipinto di blu) - Domenico Modugno (World Music)"
        ));

        genreSamples.put("Bollywood", Arrays.asList(
            "Jai Ho! - A.R. Rahman, The Pussycat Dolls (Bollywood)",
            "Chaiyya Chaiyya - Sukhwinder Singh, Sapna Awasthi (Bollywood)",
            "Dil Se Re - A.R. Rahman (Bollywood)",
            "Jashn-E-Baharaa - Javed Ali (Bollywood)",
            "Kala Chashma - Badshah, Neha Kakkar, Indeep Bakshi (Bollywood)",
            "Gallan Goodiyaan - Farhan Akhtar, Sukhwinder Singh (Bollywood)",
            "Badtameez Dil - Benny Dayal, Shefali Alvares (Bollywood)",
            "Gerua - Arijit Singh, Antara Mitra (Bollywood)",
            "Zingaat - Ajay-Atul (Bollywood)",
            "Malhari - Vishal Dadlani (Bollywood)"
        ));

        genreSamples.put("Anime OST", Arrays.asList(
            "Gurenge - LiSA (Demon Slayer OST)",
            "A Cruel Angel's Thesis - Yoko Takahashi (Neon Genesis Evangelion OST)",
            "Unravel - TK from Ling tosite sigure (Tokyo Ghoul OST)",
            "Shinzou wo Sasageyo! - Linked Horizon (Attack on Titan OST)",
            "Kimi no Na wa. (Sparkle) - RADWIMPS (Your Name. OST)",
            "My Hero Academia - You Say Run (My Hero Academia OST)",
            "Naruto Shippuden - Blue Bird (Naruto OST)",
            "Dragon Ball Z - Cha-La Head-Cha-La (Dragon Ball Z OST)",
            "One Punch Man - The Hero!! ~Ikareru Kobushi ni Hi wo Tsukero~ (One Punch Man OST)",
            "Tokyo Ghoul - Glassy Sky (Tokyo Ghoul OST)"
        ));

        genreSamples.put("Video Game OST", Arrays.asList(
            "One-Winged Angel - Final Fantasy VII (Video Game OST)",
            "Halo Theme - Halo (Video Game OST)",
            "Still Alive - Portal (Video Game OST)",
            "Megalovania - Undertale (Video Game OST)",
            "Gerudo Valley - The Legend of Zelda: Ocarina of Time (Video Game OST)",
            "Super Mario Bros. Theme - Super Mario Bros. (Video Game OST)",
            "Tetris Theme - Tetris (Video Game OST)",
            "The Witcher 3: Wild Hunt - The Wolven Storm (Video Game OST)",
            "Skyrim - Dragonborn (Video Game OST)",
            "Doom (Main Theme) - Doom (Video Game OST)"
        ));

        genreSamples.put("Film Score", Arrays.asList(
            "Hedwig's Theme - Harry Potter (Film Score)",
            "Concerning Hobbits - Lord of the Rings (Film Score)",
            "Imperial March - Star Wars (Film Score)",
            "Main Title (The Godfather) - The Godfather (Film Score)",
            "Duel of the Fates - Star Wars (Film Score)",
            "Time - Inception (Film Score)",
            "Interstellar Main Theme - Interstellar (Film Score)",
            "Pirates of the Caribbean Theme - Pirates of the Caribbean (Film Score)",
            "Jurassic Park Theme - Jurassic Park (Film Score)",
            "The Ecstasy of Gold - The Good, the Bad and the Ugly (Film Score)"
        ));

        genreSamples.put("Childrens Music", Arrays.asList(
            "Baby Shark - Pinkfong (Childrens Music)",
            "Twinkle, Twinkle, Little Star - Traditional (Childrens Music)",
            "The Wheels on the Bus - Traditional (Childrens Music)",
            "Old MacDonald Had a Farm - Traditional (Childrens Music)",
            "If You're Happy and You Know It - Traditional (Childrens Music)",
            "Head, Shoulders, Knees and Toes - Traditional (Childrens Music)",
            "Row, Row, Row Your Boat - Traditional (Childrens Music)",
            "Five Little Monkeys - Traditional (Childrens Music)",
            "Baa, Baa, Black Sheep - Traditional (Childrens Music)",
            "Mary Had a Little Lamb - Traditional (Childrens Music)"
        ));

        genreSamples.put("Holiday Music", Arrays.asList(
            "All I Want for Christmas Is You - Mariah Carey (Holiday Music)",
            "Jingle Bell Rock - Bobby Helms (Holiday Music)",
            "Rockin' Around the Christmas Tree - Brenda Lee (Holiday Music)",
            "Last Christmas - Wham! (Holiday Music)",
            "Feliz Navidad - José Feliciano (Holiday Music)",
            "It's the Most Wonderful Time of the Year - Andy Williams (Holiday Music)",
            "Let It Snow! Let It Snow! Let It Snow! - Frank Sinatra (Holiday Music)",
            "White Christmas - Bing Crosby (Holiday Music)",
            "Holly Jolly Christmas - Burl Ives (Holiday Music)",
            "Deck the Halls - Traditional (Holiday Music)"
        ));

        genreSamples.put("Spoken Word", Arrays.asList(
            "The Raven - Edgar Allan Poe (Spoken Word)",
            "Still I Rise - Maya Angelou (Spoken Word)",
            "Invictus - William Ernest Henley (Spoken Word)",
            "Do not go gentle into that good night - Dylan Thomas (Spoken Word)",
            "I Have a Dream - Martin Luther King Jr. (Spoken Word)",
            "The Road Not Taken - Robert Frost (Spoken Word)",
            "Ozymandias - Percy Bysshe Shelley (Spoken Word)",
            "If— - Rudyard Kipling (Spoken Word)",
            "Where the Sidewalk Ends - Shel Silverstein (Spoken Word)",
            "The Love Song of J. Alfred Prufrock - T.S. Eliot (Spoken Word)"
        ));

        genreSamples.put("Comedy", Arrays.asList(
            "Always Look on the Bright Side of Life - Monty Python (Comedy)",
            "The Lumberjack Song - Monty Python (Comedy)",
            "Another Brick in the Wall, Part 2 (Parody) - Weird Al Yankovic (Comedy)",
            "Eat It - Weird Al Yankovic (Comedy)",
            "The Lion Sleeps Tonight (Wimoweh) - The Tokens (Comedy)",
            "Don't Worry Be Happy - Bobby McFerrin (Comedy)",
            "Yakety Sax - Boots Randolph (Comedy)",
            "The Streak - Ray Stevens (Comedy)",
            "My Ding-a-Ling - Chuck Berry (Comedy)",
            "Fish Heads - Barnes & Barnes (Comedy)"
        ));

        genreSamples.put("Trap", Arrays.asList(
            "Trap Queen - Fetty Wap (Trap)",
            "Bad and Boujee - Migos ft. Lil Uzi Vert (Trap)",
            "Gucci Gang - Lil Pump (Trap)",
            "Sicko Mode - Travis Scott (Trap)", // También Hip Hop
            "Motorsport - Migos, Nicki Minaj, Cardi B (Trap)",
            "Panda - Desiigner (Trap)",
            "Look Alive - BlocBoy JB ft. Drake (Trap)",
            "Stir Fry - Migos ft. Travis Scott (Trap)",
            "Ric Flair Drip - Offset & Metro Boomin (Trap)",
            "Walk It Talk It - Migos ft. Drake (Trap)"
        ));

        genreSamples.put("Drill", Arrays.asList(
            "I Don't Like - Chief Keef ft. Lil Reese (Drill)",
            "Love Sosa - Chief Keef (Drill)",
            "Faneto - Chief Keef (Drill)",
            "Computers - Rowdy Rebel (Drill)",
            "Hot N*gga - Bobby Shmurda (Drill)",
            "Welcome to the Party - Pop Smoke (Drill)",
            "Dior - Pop Smoke (Drill)",
            "GATTI - Pop Smoke, Travis Scott, JackBoys (Drill)",
            "No Flockin - Kodak Black (Drill)",
            "Shmurda She Wrote - Bobby Shmurda (Drill)"
        ));

        genreSamples.put("Grime", Arrays.asList(
            "Pow! (Forward) - Lethal Bizzle (Grime)",
            "Too Many Man - Boy Better Know (Grime)",
            "German Whip - Meridian Dan ft. Big H, JME (Grime)",
            "Shutdown - Skepta (Grime)",
            "That's Not Me - Skepta ft. Jme (Grime)",
            "Man Don't Care - Jme ft. Giggs (Grime)",
            "Wiley - Wearing My Rolex (Grime)",
            "Stormzy - Shut Up (Grime)",
            "Bugzy Malone - M.E.N. (Grime)",
            "AJ Tracey - Ladbroke Grove (Grime)"
        ));

        genreSamples.put("K-R&B", Arrays.asList(
            "Crush - Crush ft. Taeyeon (K-R&B)",
            "Bermuda Triangle - Zico ft. Crush, Dean (K-R&B)",
            "D (Half Moon) - Dean ft. Gaeko (K-R&B)",
            "Pour Up - Dean ft. Zico (K-R&B)",
            "Love - Dean ft. Syd (K-R&B)",
            "Bonnie & Clyde - Dean (K-R&B)",
            "instagram - Dean (K-R&B)",
            "Sometimes - Crush (K-R&B)",
            "Don't Forget - Crush ft. Taeyeon (K-R&B)",
            "247 - Crush ft. Dean, Rado, Miso (K-R&B)"
        ));

        genreSamples.put("J-Rock", Arrays.asList(
            "Guren no Yumiya - Linked Horizon (Attack on Titan OST - J-Rock)",
            "Silhouette - KANA-BOON (Naruto Shippuden OST - J-Rock)",
            "Blue Bird - Ikimonogakari (Naruto Shippuden OST - J-Rock)",
            "Haruka Kanata - Asian Kung-Fu Generation (Naruto OST - J-Rock)",
            "The Day - Porno Graffitti (My Hero Academia OST - J-Rock)",
            "Zenzenzense - RADWIMPS (Your Name. OST - J-Rock)",
            "Sparkle - RADWIMPS (Your Name. OST - J-Rock)",
            "Crossing Field - LiSA (Sword Art Online OST - J-Rock)",
            "Ignite - Eir Aoi (Sword Art Online OST - J-Rock)",
            "Again - YUI (Fullmetal Alchemist: Brotherhood OST - J-Rock)"
        ));

        genreSamples.put("Pop Punk", Arrays.asList(
            "What's My Age Again? - Blink-182 (Pop Punk)",
            "All the Small Things - Blink-182 (Pop Punk)",
            "Misery Business - Paramore (Pop Punk)",
            "Sugar, We're Goin Down - Fall Out Boy (Pop Punk)",
            "Welcome to the Black Parade - My Chemical Romance (Pop Punk)",
            "Fat Lip - Sum 41 (Pop Punk)",
            "In Too Deep - Sum 41 (Pop Punk)",
            "Ocean Avenue - Yellowcard (Pop Punk)",
            "The Anthem - Good Charlotte (Pop Punk)",
            "Complicated - Avril Lavigne (Pop Punk)"
        ));

        genreSamples.put("Emo", Arrays.asList(
            "Welcome to the Black Parade - My Chemical Romance (Emo)", // También Pop Punk
            "Helena - My Chemical Romance (Emo)",
            "Sugar, We're Goin Down - Fall Out Boy (Emo)", // También Pop Punk
            "Misery Business - Paramore (Emo)", // También Pop Punk
            "Thnks fr th Mmrs - Fall Out Boy (Emo)",
            "I Write Sins Not Tragedies - Panic! At The Disco (Emo)",
            "The Great Escape - Boys Like Girls (Emo)",
            "Cute Without The 'E' (Cut From The Team) - Taking Back Sunday (Emo)",
            "Ohio Is For Lovers - Hawthorne Heights (Emo)",
            "Screaming Infidelities - Dashboard Confessional (Emo)"
        ));

        genreSamples.put("Folk Punk", Arrays.asList(
            "A Toast to the Future Kids! - Days N' Daze (Folk Punk)",
            "Noise Complaint - The Taxpayers (Folk Punk)",
            "Never Say Die - Pat the Bunny (Folk Punk)",
            "This City Is a Graveyard - Ghost Mice (Folk Punk)",
            "The Mountain - The Front Bottoms (Folk Punk)",
            "Twin Size Mattress - The Front Bottoms (Folk Punk)",
            "People II: The Reckoning - Andrew Jackson Jihad (Folk Punk)",
            "Bad Bad Things - AJJ (Folk Punk)",
            "The Body of an American - The Pogues (Folk Punk)",
            "Dirty Old Town - The Pogues (Folk Punk)"
        ));

        genreSamples.put("Indie Pop", Arrays.asList(
            "Pumped Up Kicks - Foster the People (Indie Pop)", // También Indie
            "Tokyo (Vampires & Wolves) - The Wombats (Indie Pop)",
            "Little Talks - Of Monsters and Men (Indie Pop)", // También Indie
            "Ho Hey - The Lumineers (Indie Pop)", // También Indie
            "Young Folks - Peter Bjorn and John (Indie Pop)",
            "Coming of Age - Foster the People (Indie Pop)",
            "Shelter Song - Temples (Indie Pop)",
            "Missed the Boat - Modest Mouse (Indie Pop)",
            "The Reeling - Passion Pit (Indie Pop)",
            "Sleepyhead - Passion Pit (Indie Pop)"
        ));

        genreSamples.put("Dream Pop", Arrays.asList(
            "Space Song - Beach House (Dream Pop)",
            "Myth - Beach House (Dream Pop)",
            "Take Care - Beach House (Dream Pop)",
            "Cherry-coloured Funk - Cocteau Twins (Dream Pop)",
            "Heaven or Las Vegas - Cocteau Twins (Dream Pop)",
            "Only Shallow - My Bloody Valentine (Dream Pop)",
            "Sometimes - My Bloody Valentine (Dream Pop)",
            "Lovesong - The Cure (Dream Pop)",
            "Just Like Heaven - The Cure (Dream Pop)",
            "Fade Into You - Mazzy Star (Dream Pop)"
        ));

        genreSamples.put("Neoclassical", Arrays.asList(
            "Nuvole Bianche - Ludovico Einaudi (Neoclassical)",
            "Experience - Ludovico Einaudi (Neoclassical)",
            "Una Mattina - Ludovico Einaudi (Neoclassical)",
            "Comptine d'un autre été, l'après-midi - Yann Tiersen (Neoclassical)",
            "La Valse d'Amélie - Yann Tiersen (Neoclassical)",
            "River Flows in You - Yiruma (Neoclassical)",
            "Kiss the Rain - Yiruma (Neoclassical)",
            "Gnossienne No. 1 - Erik Satie (Neoclassical)",
            "Gymnopédie No. 1 - Erik Satie (Neoclassical)",
            "The Heart Asks Pleasure First - Michael Nyman (Neoclassical)"
        ));

        genreSamples.put("Choral", Arrays.asList(
            "Hallelujah Chorus - Handel's Messiah (Choral)",
            "O Fortuna - Carmina Burana (Choral)",
            "Lacrimosa - Mozart's Requiem (Choral)",
            "Miserere Mei, Deus - Gregorio Allegri (Choral)",
            "Spem in alium - Thomas Tallis (Choral)",
            "Ave Maria - Franz Biebl (Choral)",
            "Lux Aeterna - Morten Lauridsen (Choral)",
            "Sure on this Shining Night - Samuel Barber (Choral)",
            "Sleep - Eric Whitacre (Choral)",
            "Cantique de Jean Racine - Gabriel Fauré (Choral)"
        ));

        genreSamples.put("New Age", Arrays.asList(
            "Orinoco Flow - Enya (New Age)",
            "Only Time - Enya (New Age)",
            "Adiemus - Adiemus (New Age)",
            "Return to Innocence - Enigma (New Age)",
            "The Mummers' Dance - Loreena McKennitt (New Age)",
            "Caribbean Blue - Enya (New Age)",
            "Shepherd Moons - Enya (New Age)",
            "Book of Days - Enya (New Age)",
            "Ameno - Era (New Age)",
            "Sadness (Part I) - Enigma (New Age)"
        ));

        genreSamples.put("Spa Music", Arrays.asList(
            "Weightless - Marconi Union (Spa Music)", // También Ambient
            "The Sound of Silence (Spa Mix) - Various Artists (Spa Music)",
            "Relaxing Rain Sounds - Nature Sounds (Spa Music)",
            "Ocean Waves for Sleep - Nature Sounds (Spa Music)",
            "Forest Sounds - Nature Sounds (Spa Music)",
            "Zen Garden - Spa Music Collection (Spa Music)",
            "Calming Water - Spa Music Collection (Spa Music)",
            "Healing Frequencies - Meditation Music (Spa Music)",
            "Soft Piano for Relaxation - Relaxing Piano Music (Spa Music)",
            "Deep Sleep Meditation - Guided Meditation (Spa Music)"
        ));

        genreSamples.put("Nature Sounds", Arrays.asList(
            "Rain Sounds for Sleep - Nature Sounds (Nature Sounds)",
            "Ocean Waves - Nature Sounds (Nature Sounds)",
            "Forest Birds - Nature Sounds (Nature Sounds)",
            "Thunderstorm Sounds - Nature Sounds (Nature Sounds)",
            "River Flow - Nature Sounds (Nature Sounds)",
            "Crackling Fireplace - Nature Sounds (Nature Sounds)",
            "Wind Chimes - Nature Sounds (Nature Sounds)",
            "Crickets Chirping - Nature Sounds (Nature Sounds)",
            "Whale Songs - Nature Sounds (Nature Sounds)",
            "Gentle Stream - Nature Sounds (Nature Sounds)"
        ));

        genreSamples.put("ASMR", Arrays.asList(
            "ASMR Tapping Sounds - ASMR Darling (ASMR)",
            "ASMR Whispering - Gentle Whispering ASMR (ASMR)",
            "ASMR Crinkling Sounds - ASMRrequests (ASMR)",
            "ASMR Scratching - ASMR Bakery (ASMR)",
            "ASMR Mouth Sounds - ASMR Glow (ASMR)",
            "ASMR Hair Brushing - ASMR Darling (ASMR)",
            "ASMR Page Turning - ASMRrequests (ASMR)",
            "ASMR Fabric Sounds - ASMR Glow (ASMR)",
            "ASMR Typing Sounds - ASMR Bakery (ASMR)",
            "ASMR Rain on Window - ASMR Darling (ASMR)"
        ));

        genreSamples.put("Educational Music", Arrays.asList(
            "The Alphabet Song - Traditional (Educational Music)",
            "Twinkle, Twinkle, Little Star - Traditional (Educational Music)",
            "Numbers Song - Kids Learning Tube (Educational Music)",
            "Days of the Week Song - The Singing Walrus (Educational Music)",
            "Months of the Year Song - Kids Learning Tube (Educational Music)",
            "The Solar System Song - Kids Learning Tube (Educational Music)",
            "Bones, Bones, Bones - The Kiboomers (Educational Music)",
            "Five Senses Song - The Singing Walrus (Educational Music)",
            "Shapes Song - Kids Learning Tube (Educational Music)",
            "Colors Song - The Singing Walrus (Educational Music)"
        ));

        genreSamples.put("Novelty Songs", Arrays.asList(
            "The Hamsterdance Song - Hampton the Hamster (Novelty Songs)",
            "What Does the Fox Say? - Ylvis (Novelty Songs)",
            "Gangnam Style - PSY (Novelty Songs)", // También K-Pop
            "Cotton Eye Joe - Rednex (Novelty Songs)",
            "The Macarena - Los del Río (Novelty Songs)",
            "Who Let The Dogs Out - Baha Men (Novelty Songs)",
            "Axel F (Crazy Frog) - Harold Faltermeyer (Novelty Songs)",
            "Barbie Girl - Aqua (Novelty Songs)",
            "Peanut Butter Jelly Time - Buckwheat Boyz (Novelty Songs)",
            "Numa Numa - O-Zone (Novelty Songs)"
        ));

        genreSamples.put("Political Hip Hop", Arrays.asList(
            "Fight the Power - Public Enemy (Political Hip Hop)",
            "Changes - 2Pac (Political Hip Hop)",
            "Alright - Kendrick Lamar (Political Hip Hop)",
            "Killing in the Name - Rage Against The Machine (Political Hip Hop)",
            "F**k tha Police - N.W.A (Political Hip Hop)",
            "The Message - Grandmaster Flash & The Furious Five (Political Hip Hop)",
            "White America - Eminem (Political Hip Hop)",
            "Sound of da Police - KRS-One (Political Hip Hop)",
            "Mississippi Goddam - Nina Simone (Political Hip Hop)",
            "Inner City Blues (Make Me Wanna Holler) - Marvin Gaye (Political Hip Hop)"
        ));

        genreSamples.put("Power Metal", Arrays.asList(
            "The Bard's Song (In the Forest) - Blind Guardian (Power Metal)",
            "Valhalla - Blind Guardian (Power Metal)",
            "Keeper of the Seven Keys - Helloween (Power Metal)",
            "Eagleheart - Stratovarius (Power Metal)",
            "Future World - Helloween (Power Metal)",
            "Emerald Sword - Rhapsody of Fire (Power Metal)",
            "I Want Out - Helloween (Power Metal)",
            "Carry On - Angra (Power Metal)",
            "Holy Thunderforce - Rhapsody of Fire (Power Metal)",
            "Hunting High and Low - Stratovarius (Power Metal)"
        ));

        genreSamples.put("Viking Metal", Arrays.asList(
            "Twilight of the Thunder God - Amon Amarth (Viking Metal)",
            "Guardians of Asgaard - Amon Amarth (Viking Metal)",
            "Raise Your Horns - Amon Amarth (Viking Metal)",
            "Father of the Wolf - Amon Amarth (Viking Metal)",
            "Valhall Awaits Me - Amon Amarth (Viking Metal)",
            "Death in Fire - Amon Amarth (Viking Metal)",
            "The Pursuit of Vikings - Amon Amarth (Viking Metal)",
            "Runes to My Memory - Amon Amarth (Viking Metal)",
            "With Oden on Our Side - Amon Amarth (Viking Metal)",
            "Deceiver of the Gods - Amon Amarth (Viking Metal)"
        ));

        genreSamples.put("Pirate Metal", Arrays.asList(
            "Keelhauled - Alestorm (Pirate Metal)",
            "Shipwrecked - Alestorm (Pirate Metal)",
            "Drink - Alestorm (Pirate Metal)",
            "Captain Morgan's Revenge - Alestorm (Pirate Metal)",
            "Fucked with an Anchor - Alestorm (Pirate Metal)",
            "Nancy the Tavern Wench - Alestorm (Pirate Metal)",
            "The Sunk'n Norwegian - Alestorm (Pirate Metal)",
            "Curse of the Crystal Coconut - Alestorm (Pirate Metal)",
            "No Grave But The Sea - Alestorm (Pirate Metal)",
            "Hang and Draw - Alestorm (Pirate Metal)"
        ));

        genreSamples.put("Medieval Metal", Arrays.asList(
            "In Taberna - Corvus Corax (Medieval Metal)",
            "Saltarello - Corvus Corax (Medieval Metal)",
            "Stella Splendens - Corvus Corax (Medieval Metal)",
            "Ave Maria - Corvus Corax (Medieval Metal)",
            "Cantus Buranus - Corvus Corax (Medieval Metal)",
            "Morgenstern - In Extremo (Medieval Metal)",
            "Spielmannsfluch - In Extremo (Medieval Metal)",
            "Herr Mannelig - In Extremo (Medieval Metal)",
            "Omnia Sol Temperat - Corvus Corax (Medieval Metal)",
            "Gaudete - Corvus Corax (Medieval Metal)"
        ));

        genreSamples.put("Pagan Metal", Arrays.asList(
            "Korpiklaani - Vodka (Pagan Metal)",
            "Ensiferum - Iron (Pagan Metal)",
            "Eluveitie - Inis Mona (Pagan Metal)",
            "Fintroll - Trollhammaren (Pagan Metal)",
            "Moonsorrow - Sankaritarina (Pagan Metal)",
            "Arkona - Yarilo (Pagan Metal)",
            "Tyr - By the Sword in My Hand (Pagan Metal)",
            "Wintersun - Sons of Winter and Stars (Pagan Metal)",
            "Equilibrium - Waldschrein (Pagan Metal)",
            "Korpiklaani - Beer Beer (Pagan Metal)"
        ));

        genreSamples.put("Blackened Thrash Metal", Arrays.asList(
            "Total Destruction - Desaster (Blackened Thrash Metal)",
            "Bestial Lust - Aura Noir (Blackened Thrash Metal)",
            "In League with Satan - Venom (Blackened Thrash Metal)",
            "Black Metal - Venom (Blackened Thrash Metal)",
            "Countess Bathory - Venom (Blackened Thrash Metal)",
            "Sacrifice - Bathory (Blackened Thrash Metal)",
            "Warfare - Midnight (Blackened Thrash Metal)",
            "Unholy and Rotten - Midnight (Blackened Thrash Metal)",
            "Satanic Royalty - Midnight (Blackened Thrash Metal)",
            "Black Metal Warfare - Desaster (Blackened Thrash Metal)"
        ));

        genreSamples.put("Technical Death Metal", Arrays.asList(
            "Crystal Mountain - Death (Technical Death Metal)",
            "Lack of Comprehension - Death (Technical Death Metal)",
            "The Philosopher - Death (Technical Death Metal)",
            "Cosmic Sea - Death (Technical Death Metal)",
            "Flattening of Emotions - Death (Technical Death Metal)",
            "The Sound of Perseverance - Death (Technical Death Metal)",
            "Perennial Quest - Death (Technical Death Metal)",
            "Voice of the Soul - Death (Technical Death Metal)",
            "Spirit Crusher - Death (Technical Death Metal)",
            "Symbolic - Death (Technical Death Metal)"
        ));

        genreSamples.put("Brutal Death Metal", Arrays.asList(
            "Hammer Smashed Face - Cannibal Corpse (Brutal Death Metal)",
            "Evisceration Plague - Cannibal Corpse (Brutal Death Metal)",
            "Decapitation Fornication - Disgorge (Brutal Death Metal)",
            "Mangled - Cannibal Corpse (Brutal Death Metal)",
            "Stripped, Raped, and Strangled - Cannibal Corpse (Brutal Death Metal)",
            "Fucked with a Knife - Cannibal Corpse (Brutal Death Metal)",
            "The Wretched Spawn - Cannibal Corpse (Brutal Death Metal)",
            "Unleashing the Bloodthirsty - Cannibal Corpse (Brutal Death Metal)",
            "Vomit the Soul - Cannibal Corpse (Brutal Death Metal)",
            "Pounded into Dust - Cannibal Corpse (Brutal Death Metal)"
        ));

        genreSamples.put("Slam Death Metal", Arrays.asList(
            "Hammer Smashed Face - Cannibal Corpse (Slam Death Metal)", // También Brutal Death Metal
            "Evisceration Plague - Cannibal Corpse (Slam Death Metal)", // También Brutal Death Metal
            "Mangled - Cannibal Corpse (Slam Death Metal)", // También Brutal Death Metal
            "Stripped, Raped, and Strangled - Cannibal Corpse (Slam Death Metal)", // También Brutal Death Metal
            "Fucked with a Knife - Cannibal Corpse (Slam Death Metal)", // También Brutal Death Metal
            "The Wretched Spawn - Cannibal Corpse (Slam Death Metal)", // También Brutal Death Metal
            "Unleashing the Bloodthirsty - Cannibal Corpse (Slam Death Metal)", // También Brutal Death Metal
            "Vomit the Soul - Cannibal Corpse (Slam Death Metal)", // También Brutal Death Metal
            "Pounded into Dust - Cannibal Corpse (Slam Death Metal)", // También Brutal Death Metal
            "Devoured by Vermin - Cannibal Corpse (Slam Death Metal)"
        ));

        genreSamples.put("Progressive Death Metal", Arrays.asList(
            "Crystal Mountain - Death (Progressive Death Metal)", // También Technical Death Metal
            "Lack of Comprehension - Death (Progressive Death Metal)", // También Technical Death Metal
            "The Philosopher - Death (Progressive Death Metal)", // También Technical Death Metal
            "Cosmic Sea - Death (Progressive Death Metal)", // También Technical Death Metal
            "Flattening of Emotions - Death (Progressive Death Metal)", // También Technical Death Metal
            "The Sound of Perseverance - Death (Progressive Death Metal)", // También Technical Death Metal
            "Perennial Quest - Death (Progressive Death Metal)", // También Technical Death Metal
            "Voice of the Soul - Death (Progressive Death Metal)", // También Technical Death Metal
            "Spirit Crusher - Death (Progressive Death Metal)", // También Technical Death Metal
            "Symbolic - Death (Progressive Death Metal)" // También Technical Death Metal
        ));

        genreSamples.put("Melodic Death Metal", Arrays.asList(
            "Blinded by Fear - At the Gates (Melodic Death Metal)",
            "Slaughter of the Soul - At the Gates (Melodic Death Metal)",
            "Only for the Weak - In Flames (Melodic Death Metal)",
            "Cloud Connected - In Flames (Melodic Death Metal)",
            "My Sweet Shadow - In Flames (Melodic Death Metal)",
            "The Gallery - Dark Tranquillity (Melodic Death Metal)",
            "Damage Done - Dark Tranquillity (Melodic Death Metal)",
            "Fiction - Dark Tranquillity (Melodic Death Metal)",
            "The Treason Wall - Dark Tranquillity (Melodic Death Metal)",
            "Insipid 2000 - Dark Tranquillity (Melodic Death Metal)"
        ));

        genreSamples.put("Funeral Doom Metal", Arrays.asList(
            "The Dreadful Hours - My Dying Bride (Funeral Doom Metal)",
            "For My Fallen Angel - My Dying Bride (Funeral Doom Metal)",
            "The Cry of Mankind - My Dying Bride (Funeral Doom Metal)",
            "A Kiss to Remeber - My Dying Bride (Funeral Doom Metal)",
            "The Thrash of Naked Limbs - My Dying Bride (Funeral Doom Metal)",
            "The Raven - My Dying Bride (Funeral Doom Metal)",
            "The Light at the End of the World - My Dying Bride (Funeral Doom Metal)",
            "The Wreckage of My Flesh - My Dying Bride (Funeral Doom Metal)",
            "The Price of Beauty - My Dying Bride (Funeral Doom Metal)",
            "The Lies I Tell - My Dying Bride (Funeral Doom Metal)"
        ));

        genreSamples.put("Atmospheric Black Metal", Arrays.asList(
            "Lost Wisdom - Burzum (Atmospheric Black Metal)",
            "Ea, Lord of the Depths - Burzum (Atmospheric Black Metal)",
            "Feeble Screams from Forests Unknown - Burzum (Atmospheric Black Metal)",
            "Det som engang var - Burzum (Atmospheric Black Metal)",
            "Hvis lyset tar oss - Burzum (Atmospheric Black Metal)",
            "Filosofem - Burzum (Atmospheric Black Metal)",
            "Dunkelheit - Burzum (Atmospheric Black Metal)",
            "Jesu Død - Burzum (Atmospheric Black Metal)",
            "War - Burzum (Atmospheric Black Metal)",
            "Black Spell of Destruction - Burzum (Atmospheric Black Metal)"
        ));

        genreSamples.put("Depressive Suicidal Black Metal (DSBM)", Arrays.asList(
            "Suicide Is Painless - Shining (DSBM)",
            "Fjara - Sólstafir (DSBM)",
            "Black Heart - Xasthur (DSBM)",
            "To the Night - Xasthur (DSBM)",
            "The Gates of Sleep - Xasthur (DSBM)",
            "A Shadow of My Future Self - Xasthur (DSBM)",
            "The Cold Earth Slept Below - Xasthur (DSBM)",
            "All the Coldness - Xasthur (DSBM)",
            "Nocturnal Poisoning - Xasthur (DSBM)",
            "Funeral of the World - Xasthur (DSBM)"
        ));

        genreSamples.put("Post-Black Metal", Arrays.asList(
            "Sunbather - Deafheaven (Post-Black Metal)",
            "Dream House - Deafheaven (Post-Black Metal)",
            "Brought to the Water - Deafheaven (Post-Black Metal)",
            "Come Back - Deafheaven (Post-Black Metal)",
            "The Pecan Tree - Deafheaven (Post-Black Metal)",
            "Vertigo - Alcest (Post-Black Metal)",
            "Autre Temps - Alcest (Post-Black Metal)",
            "Souvenirs d'un autre monde - Alcest (Post-Black Metal)",
            "Écailles de Lune - Alcest (Post-Black Metal)",
            "Kodama - Alcest (Post-Black Metal)"
        ));

        genreSamples.put("Raw Black Metal", Arrays.asList(
            "Transilvanian Hunger - Darkthrone (Raw Black Metal)",
            "Under a Funeral Moon - Darkthrone (Raw Black Metal)",
            "A Blaze in the Northern Sky - Darkthrone (Raw Black Metal)",
            "Total Death - Darkthrone (Raw Black Metal)",
            "Panzerfaust - Darkthrone (Raw Black Metal)",
            "Goatlord - Darkthrone (Raw Black Metal)",
            "Arctic Thunder - Darkthrone (Raw Black Metal)",
            "Old Star - Darkthrone (Raw Black Metal)",
            "Eternal Hails...... - Darkthrone (Raw Black Metal)",
            "Astral Fortress - Darkthrone (Raw Black Metal)"
        ));

        genreSamples.put("Blackgaze", Arrays.asList(
            "Sunbather - Deafheaven (Blackgaze)", // También Post-Black Metal
            "Dream House - Deafheaven (Blackgaze)", // También Post-Black Metal
            "Brought to the Water - Deafheaven (Blackgaze)", // También Post-Black Metal
            "Come Back - Deafheaven (Blackgaze)", // También Post-Black Metal
            "The Pecan Tree - Deafheaven (Blackgaze)", // También Post-Black Metal
            "Vertigo - Alcest (Blackgaze)", // También Post-Black Metal
            "Autre Temps - Alcest (Blackgaze)", // También Post-Black Metal
            "Souvenirs d'un autre monde - Alcest (Blackgaze)", // También Post-Black Metal
            "Écailles de Lune - Alcest (Blackgaze)", // También Post-Black Metal
            "Kodama - Alcest (Blackgaze)" // También Post-Black Metal
        ));

        genreSamples.put("Industrial Black Metal", Arrays.asList(
            "The Grand Declaration of War - Mayhem (Industrial Black Metal)",
            "My Death - Mayhem (Industrial Black Metal)",
            "Illuminate - Mayhem (Industrial Black Metal)",
            "A View from Nihil - Mayhem (Industrial Black Metal)",
            "Completion in Science of Agony - Mayhem (Industrial Black Metal)",
            "Symbols of Light - Mayhem (Industrial Black Metal)",
            "In the Lies Where Upon You Lay - Mayhem (Industrial Black Metal)",
            "A Bloodstained Cross - Mayhem (Industrial Black Metal)",
            "Death of a Millennium - Mayhem (Industrial Black Metal)",
            "Fall of Seraphs - Mayhem (Industrial Black Metal)"
        ));

        genreSamples.put("Folk Black Metal", Arrays.asList(
            "Korpiklaani - Vodka (Folk Black Metal)", // También Pagan Metal
            "Ensiferum - Iron (Folk Black Metal)", // También Pagan Metal
            "Eluveitie - Inis Mona (Folk Black Metal)", // También Pagan Metal
            "Fintroll - Trollhammaren (Folk Black Metal)", // También Pagan Metal
            "Moonsorrow - Sankaritarina (Folk Black Metal)", // También Pagan Metal
            "Arkona - Yarilo (Folk Black Metal)", // También Pagan Metal
            "Tyr - By the Sword in My Hand (Folk Black Metal)", // También Pagan Metal
            "Wintersun - Sons of Winter and Stars (Folk Black Metal)", // También Pagan Metal
            "Equilibrium - Waldschrein (Folk Black Metal)", // También Pagan Metal
            "Korpiklaani - Beer Beer (Folk Black Metal)" // También Pagan Metal
        ));

        genreSamples.put("War Metal", Arrays.asList(
            "F.O.A.D. - Goatwhore (War Metal)",
            "Apocalyptic Desolation - Blasphemy (War Metal)",
            "Gods of War - Blasphemy (War Metal)",
            "Ritual - Blasphemy (War Metal)",
            "Total Destruction - Desaster (War Metal)", // También Blackened Thrash Metal
            "Bestial Lust - Aura Noir (War Metal)", // También Blackened Thrash Metal
            "In League with Satan - Venom (War Metal)", // También Blackened Thrash Metal
            "Black Metal - Venom (War Metal)", // También Blackened Thrash Metal
            "Countess Bathory - Venom (War Metal)", // También Blackened Thrash Metal
            "Sacrifice - Bathory (War Metal)" // También Blackened Thrash Metal
        ));

        genreSamples.put("Powerviolence", Arrays.asList(
            "You Suffer - Napalm Death (Powerviolence)",
            "Scum - Napalm Death (Powerviolence)",
            "From Enslavement to Obliteration - Napalm Death (Powerviolence)",
            "Harmony Corruption - Napalm Death (Powerviolence)",
            "Utopia Banished - Napalm Death (Powerviolence)",
            "Fear, Emptiness, Despair - Napalm Death (Powerviolence)",
            "Diatribes - Napalm Death (Powerviolence)",
            "Inside the Torn Apart - Napalm Death (Powerviolence)",
            "Words from the Exit Wound - Napalm Death (Powerviolence)",
            "Enemy of the Music Business - Napalm Death (Powerviolence)"
        ));

        genreSamples.put("No Wave", Arrays.asList(
            "Contort Yourself - James Chance and the Contortions (No Wave)",
            "Baby Doll - Teenage Jesus and the Jerks (No Wave)",
            "Disgust - DNA (No Wave)",
            "The Agony of the Agonized - DNA (No Wave)",
            "Egomaniac's Kiss - DNA (No Wave)",
            "The Empty House - DNA (No Wave)",
            "The Sound of Music - DNA (No Wave)",
            "The Fear - DNA (No Wave)",
            "The Pain - DNA (No Wave)",
            "The End - DNA (No Wave)"
        ));

        genreSamples.put("Free Improvisation", Arrays.asList(
            "Machine Gun - Peter Brötzmann Octet (Free Improvisation)",
            "Karyobin - Derek Bailey (Free Improvisation)",
            "The Topography of the Lungs - Evan Parker (Free Improvisation)",
            "Company 1 - Derek Bailey (Free Improvisation)",
            "Company 2 - Derek Bailey (Free Improvisation)",
            "Company 3 - Derek Bailey (Free Improvisation)",
            "Company 4 - Derek Bailey (Free Improvisation)",
            "Company 5 - Derek Bailey (Free Improvisation)",
            "Company 6 - Derek Bailey (Free Improvisation)",
            "Company 7 - Derek Bailey (Free Improvisation)"
        ));

        genreSamples.put("Experimental Rock", Arrays.asList(
            "I Am the Walrus - The Beatles (Experimental Rock)",
            "Tomorrow Never Knows - The Beatles (Experimental Rock)",
            "Revolution 9 - The Beatles (Experimental Rock)",
            "Sister Ray - The Velvet Underground (Experimental Rock)",
            "Heroin - The Velvet Underground (Experimental Rock)",
            "Venus in Furs - The Velvet Underground (Experimental Rock)",
            "The End - The Doors (Experimental Rock)",
            "Light My Fire - The Doors (Experimental Rock)",
            "Riders on the Storm - The Doors (Experimental Rock)",
            "The Crystal Ship - The Doors (Experimental Rock)"
        ));

        genreSamples.put("Avant-garde Metal", Arrays.asList(
            "The Great Southern Trendkill - Pantera (Avant-garde Metal)",
            "Vulgar Display of Power - Pantera (Avant-garde Metal)",
            "Far Beyond Driven - Pantera (Avant-garde Metal)",
            "Cowboys from Hell - Pantera (Avant-garde Metal)",
            "Reinventing the Steel - Pantera (Avant-garde Metal)",
            "A Vulgar Display of Pantera - Pantera (Avant-garde Metal)",
            "The Art of Shredding - Pantera (Avant-garde Metal)",
            "The Sleep - Pantera (Avant-garde Metal)",
            "The Badge - Pantera (Avant-garde Metal)",
            "The Underground in America - Pantera (Avant-garde Metal)"
        ));

        genreSamples.put("Drone Metal", Arrays.asList(
            "Monoliths & Dimensions - Sunn O))) (Drone Metal)",
            "Black One - Sunn O))) (Drone Metal)",
            "Flight of the Behemoth - Sunn O))) (Drone Metal)",
            "The Grimmrobe Demos - Sunn O))) (Drone Metal)",
            "White1 - Sunn O))) (Drone Metal)",
            "White2 - Sunn O))) (Drone Metal)",
            "Altar - Sunn O))) & Boris (Drone Metal)",
            "Oracle - Sunn O))) (Drone Metal)",
            "Kannon - Sunn O))) (Drone Metal)",
            "Life Metal - Sunn O))) (Drone Metal)"
        ));

        genreSamples.put("Sludgecore", Arrays.asList(
            "Take as Needed for Pain - Eyehategod (Sludgecore)",
            "Dopesick - Acid Bath (Sludgecore)",
            "When the Kite String Pops - Acid Bath (Sludgecore)",
            "Confederacy of Ruined Lives - Eyehategod (Sludgecore)",
            "In the Name of Suffering - Eyehategod (Sludgecore)",
            "A History of Nomadic Behavior - Eyehategod (Sludgecore)",
            "The Age of Quarrel - Cro-Mags (Sludgecore)",
            "Best Wishes - Cro-Mags (Sludgecore)",
            "Alpha Omega - Cro-Mags (Sludgecore)",
            "Near Death Experience - Cro-Mags (Sludgecore)"
        ));

        genreSamples.put("Post-Metal", Arrays.asList(
            "Panopticon - Isis (Post-Metal)",
            "Oceanic - Isis (Post-Metal)",
            "Wavering Radiant - Isis (Post-Metal)",
            "In the Absence of Truth - Pelican (Post-Metal)",
            "City of Echoes - Pelican (Post-Metal)",
            "Red Sea - Russian Circles (Post-Metal)",
            "Geneva - Russian Circles (Post-Metal)",
            "Station - Russian Circles (Post-Metal)",
            "Enter - Russian Circles (Post-Metal)",
            "Empros - Russian Circles (Post-Metal)"
        ));

        genreSamples.put("Stoner Doom", Arrays.asList(
            "Dopesmoker - Sleep (Stoner Doom)",
            "Holy Mountain - Sleep (Stoner Doom)",
            "Dragonaut - Sleep (Stoner Doom)",
            "Electric Wizard - Dopethrone (Stoner Doom)",
            "Come My Fanatics... - Electric Wizard (Stoner Doom)",
            "Funeralopolis - Electric Wizard (Stoner Doom)",
            "Witchcult Today - Electric Wizard (Stoner Doom)",
            "We Live - Electric Wizard (Stoner Doom)",
            "Black Masses - Electric Wizard (Stoner Doom)",
            "Satanic Rites of Drugula - Electric Wizard (Stoner Doom)"
        ));

        genreSamples.put("Psychedelic Doom", Arrays.asList(
            "Dopesmoker - Sleep (Psychedelic Doom)", // También Stoner Doom
            "Holy Mountain - Sleep (Psychedelic Doom)", // También Stoner Doom
            "Dragonaut - Sleep (Psychedelic Doom)", // También Stoner Doom
            "Electric Wizard - Dopethrone (Psychedelic Doom)", // También Stoner Doom
            "Come My Fanatics... - Electric Wizard (Psychedelic Doom)", // También Stoner Doom
            "Funeralopolis - Electric Wizard (Psychedelic Doom)", // También Stoner Doom
            "Witchcult Today - Electric Wizard (Psychedelic Doom)", // También Stoner Doom
            "We Live - Electric Wizard (Psychedelic Doom)", // También Stoner Doom
            "Black Masses - Electric Wizard (Psychedelic Doom)", // También Stoner Doom
            "Satanic Rites of Drugula - Electric Wizard (Psychedelic Doom)" // También Stoner Doom
        ));

        genreSamples.put("Traditional Doom Metal", Arrays.asList(
            "Black Sabbath - Black Sabbath (Traditional Doom Metal)",
            "War Pigs - Black Sabbath (Traditional Doom Metal)", // También Heavy Metal
            "Children of the Grave - Black Sabbath (Traditional Doom Metal)",
            "Electric Funeral - Black Sabbath (Traditional Doom Metal)",
            "Sweet Leaf - Black Sabbath (Traditional Doom Metal)",
            "Into the Void - Black Sabbath (Traditional Doom Metal)",
            "Sabbath Bloody Sabbath - Black Sabbath (Traditional Doom Metal)",
            "Iron Man - Black Sabbath (Traditional Doom Metal)", // También Heavy Metal
            "Paranoid - Black Sabbath (Traditional Doom Metal)", // También Heavy Metal
            "N.I.B. - Black Sabbath (Traditional Doom Metal)"
        ));

        genreSamples.put("Epic Doom Metal", Arrays.asList(
            "Solitude - Candlemass (Epic Doom Metal)",
            "Bearer of Pain - Candlemass (Epic Doom Metal)",
            "Demon's Gate - Candlemass (Epic Doom Metal)",
            "At the Gallows End - Candlemass (Epic Doom Metal)",
            "A Cry from the Crypt - Candlemass (Epic Doom Metal)",
            "Dark Are the Veils of Death - Candlemass (Epic Doom Metal)",
            "Mirror Mirror - Candlemass (Epic Doom Metal)",
            "The Well of Souls - Candlemass (Epic Doom Metal)",
            "Samarithan - Candlemass (Epic Doom Metal)",
            "Under the Oak - Candlemass (Epic Doom Metal)"
        ));

        genreSamples.put("Folk Doom Metal", Arrays.asList(
            "The Dreadful Hours - My Dying Bride (Folk Doom Metal)", // También Funeral Doom Metal
            "For My Fallen Angel - My Dying Bride (Folk Doom Metal)", // También Funeral Doom Metal
            "The Cry of Mankind - My Dying Bride (Folk Doom Metal)", // También Funeral Doom Metal
            "A Kiss to Remember - My Dying Bride (Folk Doom Metal)", // También Funeral Doom Metal
            "The Thrash of Naked Limbs - My Dying Bride (Folk Doom Metal)", // También Funeral Doom Metal
            "The Raven - My Dying Bride (Folk Doom Metal)", // También Funeral Doom Metal
            "The Light at the End of the World - My Dying Bride (Folk Doom Metal)", // También Funeral Doom Metal
            "The Wreckage of My Flesh - My Dying Bride (Folk Doom Metal)", // También Funeral Doom Metal
            "The Price of Beauty - My Dying Bride (Folk Doom Metal)", // También Funeral Doom Metal
            "The Lies I Tell - My Dying Bride (Folk Doom Metal)" // También Funeral Doom Metal
        ));

        genreSamples.put("Progressive Doom Metal", Arrays.asList(
            "Blackwater Park - Opeth (Progressive Doom Metal)",
            "Ghost of Perdition - Opeth (Progressive Doom Metal)",
            "Deliverance - Opeth (Progressive Doom Metal)",
            "The Drapery Falls - Opeth (Progressive Doom Metal)",
            "Bleak - Opeth (Progressive Doom Metal)",
            "Harvest - Opeth (Progressive Doom Metal)",
            "Windowpane - Opeth (Progressive Doom Metal)",
            "The Grand Conjuration - Opeth (Progressive Doom Metal)",
            "Heir Apparent - Opeth (Progressive Doom Metal)",
            "Sorceress - Opeth (Progressive Doom Metal)"
        ));

        genreSamples.put("Sludge Doom Metal", Arrays.asList(
            "Take as Needed for Pain - Eyehategod (Sludge Doom Metal)", // También Sludgecore
            "Dopesick - Acid Bath (Sludge Doom Metal)", // También Sludgecore
            "When the Kite String Pops - Acid Bath (Sludge Doom Metal)", // También Sludgecore
            "Confederacy of Ruined Lives - Eyehategod (Sludge Doom Metal)", // También Sludgecore
            "In the Name of Suffering - Eyehategod (Sludge Doom Metal)", // También Sludgecore
            "A History of Nomadic Behavior - Eyehategod (Sludge Doom Metal)", // También Sludgecore
            "The Age of Quarrel - Cro-Mags (Sludge Doom Metal)", // También Sludgecore
            "Best Wishes - Cro-Mags (Sludge Doom Metal)", // También Sludgecore
            "Alpha Omega - Cro-Mags (Sludge Doom Metal)", // También Sludgecore
            "Near Death Experience - Cro-Mags (Sludge Doom Metal)" // También Sludgecore
        ));

        // Asegurarse de que todos los géneros que puedan ser devueltos por la API tengan canciones
        for (String genre : genres) {
            songs.addAll(genreSamples.getOrDefault(genre, Collections.emptyList()));
        }

        // Eliminar duplicados si un género se solicita varias veces
        return songs.stream().distinct().collect(Collectors.toList());
    }
}
