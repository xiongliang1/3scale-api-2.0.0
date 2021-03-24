package com.hisense.gateway.management.web.interceptor;

import com.hisense.gateway.library.model.dto.web.ProjectInfo;
import lombok.Data;

import java.util.List;

@Data
final class ProjectInfoBody {
    List<ProjectInfo> data;
}
