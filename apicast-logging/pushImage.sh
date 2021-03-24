function apicast_image_push(){
	ID=`docker ps -a |grep 'k8s_apicast-staging_apicast-staging'|awk 'NR==1'|awk '{print $1}'`;echo $ID
	ID=`docker ps -a |grep 'k8s_apicast-production'|awk 'NR==1'|awk '{print $1}'`;echo $ID
	docker cp ./gateway/src ${ID}:/opt/app-root/src/
	docker cp ./gateway/conf ${ID}:/opt/app-root/src/

	APICAST=/opt/app-root/src/src/apicast;
	docker exec -it ${ID} cat /opt/app-root/src/conf/nginx.conf.liquid|grep mingguilai
	docker exec -it ${ID} ls ${APICAST}/policy_chain.lua
	docker exec -it ${ID} ls ${APICAST}/policy/hisense_log_config/apicast-policy.json
	docker exec -it ${ID} ls ${APICAST}/policy/hisense_log_config/hisense_log_config.lua
	docker exec -it ${ID} ls ${APICAST}/policy/hisense_log_config/init.lua
	docker exec -it ${ID} ls ${APICAST}/policy/hisense_log_writer/hisense_log_writer.lua
	docker exec -it ${ID} ls ${APICAST}/policy/hisense_log_writer/init.lua
	docker exec -it ${ID} ls ${APICAST}/policy/hisense_log_writer/log_writer.lua
	docker exec -it ${ID} ls ${APICAST}/policy/hisense_log_writer/luaJson.lua
	docker exec -it ${ID} ls ${APICAST}/policy/hisense_log_writer/table_util.lua
	
	docker commit -m "update" -a "hisense" ${ID} registry.hisense.com/3scale-apicast/apicast-gateway:2.6.1
	docker images|grep apicast-gateway;
	
}

apicast_image_push
