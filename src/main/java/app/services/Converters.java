package app.services;

import app.dtos.GuideDTO;
import app.dtos.TripDTO;
import app.entities.Guide;
import app.entities.Trip;

import java.util.stream.Collectors;

public class Converters {


    public Trip convertTripDTOToTrip(TripDTO tripDTO){
        Trip trip = Trip.builder()
                .name(tripDTO.getName())
                .start(tripDTO.getStart())
                .end(tripDTO.getEnd())
                .locationCoordinates(tripDTO.getLocationCoordinates())
                .price(tripDTO.getPrice())
                .category(tripDTO.getCategory())
                .guide(convertGuideDTOToGuide(tripDTO.getGuideDTO()))
                .build();
        return trip;
    }

    public Guide convertGuideDTOToGuide(GuideDTO guideDTO){
        if(guideDTO == null) return null;
        Guide guide = Guide.builder()
                .guideId(guideDTO.getGuideId())
                .name(guideDTO.getName())
                .email(guideDTO.getEmail())
                .phone(guideDTO.getPhone())
                .yearsOfExp(guideDTO.getYearsOfExp())
                .trips(guideDTO.getTripDTOs().stream().map(tripDTO -> convertTripDTOToTrip(tripDTO)).collect(Collectors.toSet()))
                .build();
        return guide;
    }



//    public User convertUserDtoToUser(UserCreateDTO userCreateDTO, EntityManagerFactory emf){
//        UserDAO userDAO = new UserDAO(emf);
//        User user = User.builder()
//                .address(userCreateDTO.getAddress())
//                .userName(userCreateDTO.getUserName())
//                .phone(userCreateDTO.getPhone())
//                .roles(userCreateDTO.getRoles().stream().map(role -> new Role(role.toUpperCase())).collect(Collectors.toSet()))
//                .password(userCreateDTO.getPassword())
//                .events(userCreateDTO.getEvents())
//                .build();
//
//        return user;
//    }
//
//    public Event convertEventDtoToEvent(EventDTO eventDTO, EntityManagerFactory emf){
//        EventDAO eventDAO = new EventDAO(emf);
//        Event event = Event.builder()
//                .category(eventDTO.getCategory())
//                .description(eventDTO.getDescription())
//                .max(eventDTO.getMax())
//                .price(eventDTO.getPrice())
//                .name(eventDTO.getName())
//                //.interrested(eventDTO.getInterrested())
//                .participants(eventDTO.getParticipants())
//                .localDateTime(eventDTO.getLocalDateTime())
//                .location(eventDTO.getLocation())
//                .build();
//        return event;
//    }

}
