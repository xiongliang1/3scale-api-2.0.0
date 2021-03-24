create or replace  view ALERTPOLICYDATA
as select item1.ITEM_NAME apiSubscribeSystem,item2.ITEM_NAME apiPublishSystem,app.INSTANCE_ID instanceId,
policy.MSG_SEND_INTERVAL msgSendInterval,policy.MSG_SEND_TYPES msgSendTypes,policy.NAME policyName,
to_char(policy.MSG_RECEIVERS||','||app.CREATOR) msgReceivers,
to_char(policy.UPDATE_TIME,'yyyy-mm-dd HH24:mi:ss') policyUpdateTime,to_char(relationship.scale_api_id)  scaleServiceId,
app.USER_KEY subscribeSystemUserKey,policy.TRIGGER_METHODS triggerMethods from PUBLISH_APPLICATION app
join PUBLISH_API api on app.API_ID = api.id and app.status=2  and app.type=1 and app.USER_KEY is not null
join ALERT_POLICY policy on api.alert_policy_id = policy.id and policy.API_IDS is not null and policy.status=1 and policy.enable=1
join DATA_ITEM item1 on app.system = item1.id
join DATA_ITEM item2 on api.system_id = item2.id
join PUBLISH_API_INSTANCE_RELATIONSHIP relationship on relationship.api_id=api.id and relationship.instance_id=app.instance_id;