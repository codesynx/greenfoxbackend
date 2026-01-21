package com.greenfox.backend.modules.ai.rag;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Curated knowledge base about Kazakhstan travel destinations.
 * These documents are embedded and stored in PGVector for RAG retrieval.
 */
@Component
public class DestinationDocuments {

    /**
     * Get all destination knowledge documents.
     */
    public List<Document> getAllDocuments() {
        List<Document> documents = new ArrayList<>();

        // Alakol Lake
        documents.add(new Document(
                "Alakol Lake is a stunning saltwater lake in eastern Kazakhstan, known for its healing properties " +
                        "and beautiful beaches. The best time to visit is from June to August when temperatures are warm " +
                        "and perfect for swimming. The lake's water has high mineral content, believed to have therapeutic " +
                        "benefits for skin conditions and respiratory issues. Popular activities include swimming, sunbathing, " +
                        "and enjoying fresh local fish. The area offers various accommodation options from budget guesthouses " +
                        "to luxury resorts. Average water temperature in summer reaches 25-28°C.",
                Map.of("destination", "Alakol", "type", "lake", "season", "summer")
        ));

        documents.add(new Document(
                "Getting to Alakol: The nearest airport is in Usharal, but most travelers fly to Almaty and take " +
                        "a domestic flight to Usharal or drive approximately 600km. The journey from Almaty takes about " +
                        "8-10 hours by car. Local transport includes taxis and buses from Usharal to the lake resorts.",
                Map.of("destination", "Alakol", "type", "transport")
        ));

        // Shymbulak
        documents.add(new Document(
                "Shymbulak is Kazakhstan's premier ski resort located in the Trans-Ili Alatau mountains, just 25km " +
                        "from Almaty city center. The resort sits at an elevation of 2,200-3,200 meters, offering excellent " +
                        "snow conditions from November to April. Shymbulak features modern ski lifts, well-groomed slopes " +
                        "for all skill levels, and stunning mountain views. In summer, it transforms into a hiking and " +
                        "mountain biking destination with cable car rides offering panoramic views.",
                Map.of("destination", "Shymbulak", "type", "ski_resort", "season", "winter")
        ));

        documents.add(new Document(
                "Shymbulak facilities include ski and snowboard rentals, professional instructors, cozy mountain " +
                        "cafes, and restaurants. The Medeu ice skating rink, the world's highest high-altitude skating " +
                        "rink at 1,691 meters, is located nearby and often visited together with Shymbulak. Cable car " +
                        "tickets and ski passes can be purchased on-site or online.",
                Map.of("destination", "Shymbulak", "type", "facilities")
        ));

        // Kapchagai
        documents.add(new Document(
                "Kapchagai is a reservoir city located 70km from Almaty, famous for its beach resorts, water sports, " +
                        "and entertainment facilities. The Kapchagai Reservoir offers warm swimming waters in summer " +
                        "(June-August) and is a popular weekend getaway for Almaty residents. The area features water " +
                        "parks, jet skiing, banana boat rides, and fishing opportunities. Several casinos and entertainment " +
                        "complexes make it a destination for nightlife enthusiasts.",
                Map.of("destination", "Kapchagai", "type", "reservoir", "season", "summer")
        ));

        documents.add(new Document(
                "Kapchagai resorts range from budget-friendly beach clubs to luxury all-inclusive properties. " +
                        "Many resorts offer private beach access, swimming pools, restaurants, and accommodation. " +
                        "The drive from Almaty takes about 1 hour, making it ideal for day trips or weekend stays.",
                Map.of("destination", "Kapchagai", "type", "accommodation")
        ));

        // Almaty
        documents.add(new Document(
                "Almaty is Kazakhstan's largest city and cultural capital, surrounded by the beautiful Tian Shan " +
                        "mountains. The city offers a mix of Soviet-era architecture, modern skyscrapers, and lush green " +
                        "parks. Key attractions include Zenkov Cathedral in Panfilov Park, the Green Bazaar, Kok-Tobe " +
                        "hill with cable car, and numerous museums. Almaty serves as the gateway to mountain adventures " +
                        "at Shymbulak, Big Almaty Lake, and Charyn Canyon.",
                Map.of("destination", "Almaty", "type", "city")
        ));

        documents.add(new Document(
                "Almaty has a continental climate with hot summers and cold winters. The best times to visit are " +
                        "spring (April-May) and autumn (September-October) for pleasant weather. The city offers " +
                        "diverse accommodation from international hotel chains to boutique hotels and apartments. " +
                        "Almaty International Airport connects to major cities worldwide.",
                Map.of("destination", "Almaty", "type", "travel_info")
        ));

        // Borovoe (Burabay)
        documents.add(new Document(
                "Borovoe (Burabay) National Park is often called 'Kazakhstan's Switzerland' due to its stunning " +
                        "alpine-like scenery. Located in northern Kazakhstan, about 250km from Astana, it features " +
                        "pristine pine forests, crystal-clear lakes, and unique rock formations. The area is popular " +
                        "year-round: summer offers swimming, hiking, and boating, while winter brings cross-country " +
                        "skiing and ice fishing.",
                Map.of("destination", "Borovoe", "type", "national_park", "season", "year-round")
        ));

        documents.add(new Document(
                "Borovoe's famous landmarks include Zhumbaktas Rock (Sphinx Rock), Okzhetpes Rock, and the beautiful " +
                        "Borovoe and Bolshoe Chebachye lakes. Sanatoriums and health resorts in the area offer wellness " +
                        "treatments and clean mountain air therapy. The region is known for its healing properties and " +
                        "attracts visitors seeking relaxation and rejuvenation.",
                Map.of("destination", "Borovoe", "type", "attractions")
        ));

        // General Kazakhstan Travel Tips
        documents.add(new Document(
                "Kazakhstan travel tips: The currency is Kazakhstani Tenge (KZT). Credit cards are widely accepted " +
                        "in cities but carry cash for rural areas. The official languages are Kazakh and Russian. " +
                        "Visa-free entry is available for many nationalities for stays up to 30 days. The country spans " +
                        "multiple time zones - Almaty is UTC+5 while Astana is UTC+5.",
                Map.of("type", "travel_tips")
        ));

        documents.add(new Document(
                "Kazakhstan is known for its hospitality and traditional cuisine. Must-try dishes include beshbarmak " +
                        "(boiled meat with noodles), plov (rice pilaf), manti (dumplings), and kumis (fermented mare's " +
                        "milk). The country offers diverse landscapes from steppes and deserts to mountains and lakes, " +
                        "making it ideal for adventure tourism and nature lovers.",
                Map.of("type", "culture_cuisine")
        ));

        return documents;
    }
}
