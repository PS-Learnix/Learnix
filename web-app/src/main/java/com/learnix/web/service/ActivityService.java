package com.learnix.web.service;

import com.learnix.web.dto.ActivityResponse;
import com.learnix.web.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    public List<ActivityResponse> getActivitiesByPeriodSp(Integer idCoursePeriod) {
        return activityRepository.getActivitiesByPeriod(idCoursePeriod);
    }

    @Transactional
    public Integer createActivitySp(Integer idCoursePeriod, String name, Double weight) {
        return activityRepository.createActivity(idCoursePeriod, name, weight);
    }
}