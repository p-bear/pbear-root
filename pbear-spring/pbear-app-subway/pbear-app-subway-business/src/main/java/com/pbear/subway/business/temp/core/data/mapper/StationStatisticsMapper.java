package com.pbear.subway.business.temp.core.data.mapper;

import com.pbear.subway.business.common.seoulopenapi.dto.ResCardSubwayStatsNew;
import com.pbear.subway.business.temp.core.document.StationStatistics;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StationStatisticsMapper {
  StationStatisticsMapper INSTANCE = Mappers.getMapper(StationStatisticsMapper.class);

  @Mapping(source = "subwayStats.useDt", target = "useDate")
  @Mapping(source = "subwayStats.ridePasgrNum", target = "ridePassengerNum")
  @Mapping(source = "subwayStats.alignPasgrNum", target = "alignPassengerNum")
  StationStatistics toStationStatistics(final ResCardSubwayStatsNew.SubwayStats subwayStats, final String stationId);
}
