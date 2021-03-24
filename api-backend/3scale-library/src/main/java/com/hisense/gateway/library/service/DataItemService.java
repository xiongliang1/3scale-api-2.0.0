package com.hisense.gateway.library.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.portal.PublishApiInfo;
import com.hisense.gateway.library.model.dto.buz.DataItemDto;
import com.hisense.gateway.library.model.pojo.base.DataItem;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DataItemService {
    List<DataItem> findDataItems(String groupKey);

    Result<List<DataItem>> searchDataItems(String itemName,String itemKey);

    List<DataItem> findDataItemsByParentId(String groupKey, Integer parentId);

    List<DataItem> searchDataItem(String groupKey,Integer parentId,String groupName,String itemKey);

    Result<Boolean> createDataItem(DataItemDto dataItemDto);

    Result<Boolean> deleteDataItems(Integer id);

    Result<Boolean> updateDataItems(Integer id, DataItemDto dataItemDto);

    Result<Page<DataItem>> searchDataItems(Integer page, Integer size);

    List<DataItem> findAllDataItems(String groupKey);

    List<DataItem> findSystemDataItems(String groupKey, Integer categoryOne, Integer categoryTwo);

    // for portal
    List<DataItem> getCateGoryOne();

    List<DataItem> searchDataItems();

    Map<String, Map<Integer,Object>> findAllDataItem(String environment,Integer partition);

    Result<List<PublishApiInfo>> findHotRecommendApi(String environment);

    List<PublishApiInfo> getCateGoryOneAndApi(Integer id,String environment);

    List<DataItem> getAllSystems(String systemName,Integer status);
}
