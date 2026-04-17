package pt.unl.fct.iadi.novaevents.controller.data

import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import pt.unl.fct.iadi.novaevents.model.Club
import pt.unl.fct.iadi.novaevents.model.ClubCategory
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.model.EventType
import pt.unl.fct.iadi.novaevents.model.Role
import pt.unl.fct.iadi.novaevents.model.AppUser
import pt.unl.fct.iadi.novaevents.repository.ClubRepository
import pt.unl.fct.iadi.novaevents.repository.EventRepository
import pt.unl.fct.iadi.novaevents.repository.EventTypeRepository
import pt.unl.fct.iadi.novaevents.repository.UserRepository
import java.time.LocalDate

@Component
class DataInitializer(
    private val clubRepository: ClubRepository,
    private val eventRepository: EventRepository,
    private val usersRepository: UserRepository,
    private val eventTypeRepository: EventTypeRepository
) : ApplicationRunner {

    override fun run(args: org.springframework.boot.ApplicationArguments?) {
        if (clubRepository.count() > 0) return // já inicializado

        val encoder = org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
        val alice = AppUser(username = "alice", password = encoder.encode("password123"))
        alice.roles.add(Role(name = "ROLE_EDITOR", appUser = alice))
        usersRepository.save(alice)

        val bob = AppUser(username = "bob", password = encoder.encode("password123"))
        bob.roles.add(Role(name = "ROLE_EDITOR", appUser = bob))
        usersRepository.save(bob)

        val charlie = AppUser(username = "charlie", password = encoder.encode("password123"))
        charlie.roles.add(Role(name = "ROLE_ADMIN", appUser = charlie))
        usersRepository.save(charlie)

        //  Event Types
        val workshop = eventTypeRepository.save(EventType(name = "WORKSHOP"))
        val competition = eventTypeRepository.save(EventType(name = "COMPETITION"))
        val social = eventTypeRepository.save(EventType(name = "SOCIAL"))
        val other = eventTypeRepository.save(EventType(name = "OTHER"))
        val talk = eventTypeRepository.save(EventType(name = "TALK"))
        val meeting = eventTypeRepository.save(EventType(name = "MEETING"))

        //  Clubs
        val chess = clubRepository.save(
            Club(
                name = "Chess Club",
                description = "A club for chess enthusiasts",
                category = ClubCategory.SOCIAL
            )
        )
        val robotics = clubRepository.save(
            Club(
                name = "Robotics Club",
                description = "The Robotics Club is the place to turn ideas into machines",
                category = ClubCategory.ACADEMIC
            )
        )
        val photography = clubRepository.save(
            Club(
                name = "Photography Club",
                description = "For photography lovers",
                category = ClubCategory.CULTURAL
            )
        )
        val hiking = clubRepository.save(
            Club(
                name = "Hiking & Outdoors Club",
                description = "Hiking and outdoor adventures",
                category = ClubCategory.SPORTS
            )
        )
        val film = clubRepository.save(
            Club(
                name = "Film Society Club",
                description = "Film screenings and discussions",
                category = ClubCategory.CULTURAL
            )
        )

        // 3️⃣ Events
        eventRepository.save(
            Event(
                name = "Beginner's Chess Workshop",
                date = LocalDate.now().plusDays(1),
                club = chess,
                type = workshop,
                location = "Room A101",

            )
        )
        eventRepository.save(
            Event(
                name = "Spring Chess Tournament",
                date = LocalDate.now().plusDays(30),
                club = chess,
                type = competition,
                location = "Room A",
            )
        )
        eventRepository.save(
            Event(
                name = "Robotics Workshop",
                date = LocalDate.now().plusDays(10),
                club = robotics,
                type = workshop,
                location = "Lab 3"
            )
        )
        eventRepository.save(
            Event(
                name = "Photo Walk",
                date = LocalDate.now().plusDays(5),
                club = photography,
                type = social,
                location = "Campus Garden"
            )
        )
        eventRepository.save(
            Event(
                name = "Hiking Trip",
                date = LocalDate.now().plusDays(20),
                club = hiking,
                type = social
            )
        )
        eventRepository.save(
            Event(
                name = "Film Screening",
                date = LocalDate.now().plusDays(15),
                club = film,
                type = other,
                location = "Auditorium"
            )
        )
    }
}