package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.entity.field_objects.Mine;
import com.example.aquaticboogaloo.repository.MineRepository;
import com.example.aquaticboogaloo.util.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MineService {

    private final MineRepository mineRepository;


    public List<Mine> findMinesAroundShip(Long gameId, int visionRadius, Point min, Point max) {
        return mineRepository.findMinesInRegion(
                gameId,
                min.x() - visionRadius,
                min.y() - visionRadius,
                max.x() + visionRadius,
                max.y() + visionRadius
        );
    }
}
