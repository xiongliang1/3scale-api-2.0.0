package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.pojo.base.DataItem;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface DataItemRepository extends CommonQueryRepository<DataItem, Integer> {

    @Query("select d from DataItem d where d.status=1 and d.groupKey='categoryOne' ")
    List<DataItem> findByCateGoryOne();

    @Query("select d from DataItem d where d.status=1 and d.groupKey='system' ")
    List<DataItem> findBySystsem();

    @Query("select d from DataItem d where d.status=1 and d.groupKey='categoryTwo' ")
    List<DataItem> findByGroupKey();

    @Query("select d.id,d.itemName from DataItem d where d.status=1 and d.groupKey<>'categoryOne'")
    List<DataItem> findTwoAndSystemDataItems();
    @Query("select d from DataItem d where d.status=1 AND d.groupKey=:groupKey")
    List<DataItem> findAllByGroupKey(@Param("groupKey") String groupKey);

    @Query("select d from DataItem d where d.status=1 AND d.parentId=:parentId and d.groupKey=:groupKey order by d.itemName")
    List<DataItem> findAllByGroupKeyAndParentId(@Param("groupKey") String groupKey, @Param("parentId") Integer parentId);

    @Query("select d from DataItem d where d.status=1 and d.parentId=:parentId and d.groupName=:groupName and d.itemName=:itemName")
    List<DataItem> findByParam(@Param("groupName") String groupName, @Param("itemName") String itemName,
                               @Param("parentId") Integer parentId);

    @Query("select d from DataItem d where d.status=1 and d.itemName=:itemName and d.itemKey=:itemKey and d.groupKey=:groupKey ")
    List<DataItem> findByKeyAndName(@Param("itemName")String itemName,@Param("itemKey")String itemKey,@Param("groupKey")String groupKey);

    @Query("select d from DataItem d where d.status=1 and d.itemName=:itemName and d.itemKey=:itemKey and d.groupKey=:groupKey and d.parentId=:parentId")
    List<DataItem> findByKeyAndParentId(@Param("itemName")String itemName,@Param("itemKey")String itemKey,@Param("groupKey")String groupKey,@Param("parentId")Integer parentId);

    @Modifying
    @Transactional
    @Query("update DataItem set status=0,updateTime=:updateTime where id=:id")
    void logicDeleteById(@Param("id") Integer id, @Param("updateTime") Date updateTime);

    @Query("select d from DataItem d where d.status=1 and d.parentId=:parentId")
    List<DataItem> findAllByParentId(@Param("parentId") Integer id);

    @Query("select d from DataItem d where d.status=1 and d.id in (:id)")
    List<DataItem> findByIds(@Param("id") List<Integer> dataItemIds);

    @Query("select d from DataItem d where d.status=1 and d.groupKey='system' and d.itemKey=:itemKey")
    List<DataItem> findByItemKey(@Param("itemKey") String itemKey);

    @Query("select d from DataItem d where  d.status=1 and d.groupKey='system' and d.itemKey=:itemKey")
    DataItem findSystemByItemKey(@Param("itemKey") String itemKey);

    @Query("select d from DataItem d where  d.status=1 and d.groupKey='system' and d.itemName like %?1%")
    List<DataItem> findSystemByItemName(@Param("itemName") String itemName);

    @Query("select d from DataItem d where  d.status=1 and d.groupKey='system' and d.itemKey in (:itemKeys)")
    List<DataItem> findSystemByItemKeys(@Param("itemKeys") List<String> itemKeys);

    @Query("select d from DataItem d join SystemInfo system on d.itemKey = system.code where system.id=:projectId")
    List<DataItem> findSystemByproject(@Param("projectId") Integer projectId);
}
