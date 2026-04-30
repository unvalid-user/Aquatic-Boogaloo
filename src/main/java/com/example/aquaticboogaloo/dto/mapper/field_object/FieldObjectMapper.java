package com.example.aquaticboogaloo.dto.mapper.field_object;

import com.example.aquaticboogaloo.config.MapStructConfig;
import com.example.aquaticboogaloo.dto.response.field.*;
import com.example.aquaticboogaloo.entity.field_objects.*;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(config = MapStructConfig.class)
public interface FieldObjectMapper {

    @Mapping(target = "playerId", source = "owner.id")
    ShipResponse toResponse(Ship ship);

    ShipCellResponse toResponse(ShipCell sc);

    @InheritConfiguration(name = "toFieldObjectResponse")
    MineResponse toResponse(Mine mine);

    @InheritConfiguration(name = "toFieldObjectResponse")
    ScanResponse toResponse(Scan scan);

    @Mapping(target = "playerId", source = "action.actor.id")
    @Mapping(target = "objectId", source = "id")
    @Mapping(target = "locationY", source = "action.locationY")
    @Mapping(target = "locationX", source = "action.locationX")
    FieldObjectResponse toFieldObjectResponse(ActionObject actionObject);
}
