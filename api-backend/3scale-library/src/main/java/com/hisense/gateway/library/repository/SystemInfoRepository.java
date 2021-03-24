package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.pojo.base.SystemInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author guilai.ming 2020/09/10
 */
public interface SystemInfoRepository extends CommonQueryRepository<SystemInfo, Integer> {
    @Query("select p from SystemInfo p where p.code=:dataItemKey")
    SystemInfo findByDataItemKey(@Param("dataItemKey") String dataItemKey);

    @Query("select p from SystemInfo p where p.name like CONCAT('%',:systemName,'%')")
    List<SystemInfo> findSystemByNameLike(@Param("systemName") String systemName);

    SystemInfo findFirstBySlmid(@Param("slmid") String slmid);

    @Query("select p from SystemInfo p where p.dataItemId=:dataItemId")
    List<SystemInfo> findBySystem(@Param("dataItemId") String dataItemId);

    @Query("select p from SystemInfo p where p.isPrdApiCreated=:isPrdApiCreated")
    List<SystemInfo> findByPrdApiCreated(@Param("isPrdApiCreated") boolean isPrdApiCreated);

    @Query("select p from SystemInfo p where p.isDevApiCreated=:isDevApiCreated")
    List<SystemInfo> findByDevApiCreated(@Param("isDevApiCreated") boolean isDevApiCreated);

    @Query("select p from SystemInfo p where p.isPrdMsgCreated=:isPrdMsgCreated")
    List<SystemInfo> findByPrdMsgCreated(@Param("isPrdMsgCreated") boolean isPrdMsgCreated);

    @Query("select p from SystemInfo p where p.isDevMsgCreated=:isDevMsgCreated")
    List<SystemInfo> findByDevMsgCreated(@Param("isDevMsgCreated") boolean isDevMsgCreated);

    @Query("select p from SystemInfo p where (p.prdApiAdminName like CONCAT('%',:userNmae,'%') or p.prdApiDevName like CONCAT('%',:userNmae,'%') " +
            "or p.prdApiTenantName like CONCAT('%',:userNmae,'%'))")
    List<SystemInfo> findSystemApiPrdByUserNameLike(String userNmae);

    @Query("select p from SystemInfo p where (p.apiAdminName like CONCAT('%',:userNmae,'%') or p.devApiDevName like CONCAT('%',:userNmae,'%') " +
            "or p.devApiTenantName like CONCAT('%',:userNmae,'%'))")
    List<SystemInfo> findSystemApiDevByUserNameLike(String userNmae);

    @Query("select p from SystemInfo p where (p.prdMsgAdminName like CONCAT('%',:userNmae,'%') or p.prdMsgDevName like CONCAT('%',:userNmae,'%'))")
    List<SystemInfo> findSystemMsgPrdByUserNameLike(String userNmae);

    @Query("select p from SystemInfo p where (p.msgAdminName like CONCAT('%',:userNmae,'%') or p.devMsgDevName like CONCAT('%',:userNmae,'%'))")
    List<SystemInfo> findSystemMsgDevByUserNameLike(String userNmae);

}
