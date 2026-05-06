package com.example.aquaticboogaloo.dto.mapper;

import com.example.aquaticboogaloo.config.MapStructConfig;
import com.example.aquaticboogaloo.dto.response.KnownCellResponse;
import com.example.aquaticboogaloo.dto.response.action.AttackHitResponse;
import com.example.aquaticboogaloo.dto.response.action.AttackResponse;
import com.example.aquaticboogaloo.entity.AttackHit;
import com.example.aquaticboogaloo.entity.field_objects.Attack;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface AttackMapper {
    @Mapping(target = "turn", source = "action.createdAtTurn")
    @Mapping(target = "objectType", expression = "java(attack.getHit() == null? null : attack.getHit().getObjectType())")
    @Mapping(target = "locationY", source = "action.locationY")
    @Mapping(target = "locationX", source = "action.locationX")
    @Mapping(target = "attackId", source = "id")
    KnownCellResponse toKnownCellResponse(Attack attack);

    @Mapping(target = "objectOwnerPlayerId", source = "objectOwner.id")
    @Mapping(target = "hitBackShipCellId", source = "mineHitBack.id")
    AttackHitResponse toResponse(AttackHit hit);

    @Mapping(target = "playerId", source = "action.actor.id")
    @Mapping(target = "locationY", source = "action.locationY")
    @Mapping(target = "locationX", source = "action.locationX")
    AttackResponse toResponse(Attack attack);
}
