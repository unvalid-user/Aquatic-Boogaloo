package com.example.aquaticboogaloo.dto.response.field;

import com.example.aquaticboogaloo.entity.field_objects.Scan;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScanResponse extends FieldObjectResponse {
    private int shipCellsNumber;
    private int minesNumber;
}
