package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.entity.field_objects.Scan;
import com.example.aquaticboogaloo.repository.ScanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScanService {

    private final ScanRepository scanRepository;


    public List<Scan> findPlayerScans(Long playerId) {
        return scanRepository.findScansByPlayerId(playerId);
    }

    public List<Scan> findGameScans(Long gameId) {
        return scanRepository.findByGame_Id(gameId);
    }
}
