export function filteRequestParams(dataBody) {//请求参数过滤
  let arr = [];
  dataBody.forEach(item => {
    let json = {
      name: item.name,
      paramType: item.location,
      dataType: item.type,
      required: item.necessary === "true" ? true : false,
      defaultValue: item.sample,
      value: item.describe
    }
    arr.push(json);
  })
  return arr
}

export function filteRequestBody(data) {//请求参数过滤
  let requestBody = {
    name: data[0].name,
    value: data[0].describe,
    dataType: data[0].type,
    defaultValue: data[0].sample,
    object: null
  };
  if (data[0].children) {
    requestBody.object = [];
    data[0].children.forEach(item => {
      let jsonLevelOne = {
        name: item.name,
        description: item.describe,
        dataType: item.type,
        defaultValue: item.sample,
        object: null
      }
      if (item.children) {
        jsonLevelOne.object = [];
        item.children.forEach(it => {
          let jsonLevelTwo = {
            name: it.name,
            description: it.describe,
            dataType: it.type,
            defaultValue: it.sample,
            object: null
          }
          if (it.children) {
            jsonLevelTwo.object = [];
            it.children.forEach(j => {
              let jsonLevelThree = {
                name: j.name,
                description: j.describe,
                dataType: j.type,
                defaultValue: j.sample,
                object: null
              }
              jsonLevelTwo.object.push(jsonLevelThree)
            })
          }
          jsonLevelOne.object.push(jsonLevelTwo)
        })
      }
      requestBody.object.push(jsonLevelOne)
    })
  }
  return requestBody
}

export function filteResponseBody(data) {//返回参数过滤
  let responseBody = {//返回参数
    name: data[0].name,
    description: data[0].describe,
    dataType: data[0].type,
    defaultValue: data[0].sample
  }
  if (data[0].children) {
    responseBody.object = [];
    data[0].children.forEach(item => {
      let jsonLevelOne = {
        name: item.name,
        description: item.describe,
        dataType: item.type,
        defaultValue: item.sample
      }
      if (item.children) {
        jsonLevelOne.object = [];
        item.children.forEach(it => {
          let jsonLevelTwo = {
            name: it.name,
            description: it.describe,
            dataType: it.type,
            defaultValue: it.sample
          }
          if (it.children) {
            jsonLevelTwo.object = [];
            it.children.forEach(j => {
              let jsonLevelThree = {
                name: j.name,
                description: j.describe,
                dataType: j.type,
                defaultValue: j.sample
              }
              jsonLevelTwo.object.push(jsonLevelThree)
            })
          }
          jsonLevelOne.object.push(jsonLevelTwo)
        })
      }
      responseBody.object.push(jsonLevelOne)
    })
  }
  return responseBody
}

export function filterText(data) {//把上传文件里面的内容转换成json
  console.log(data);
  let arr = [];
  let i = -1;
  for (let key in data) {
    let json = {};
    i++;
    json.key = 0 + "-" + i;
    json.node = i;
    json.name = key;
    json.level = 1;
    if (Object.prototype.toString.call(data[key]) === '[object String]') {
      json.type = 'string';
      json.sample = data[key].replace(/\“/g, "").replace(/\”/g, "");
    } else if (Object.prototype.toString.call(data[key]) === '[object Number]') {
      json.type = 'int';
      json.sample = data[key] + "";
    } else if (Object.prototype.toString.call(data[key]) === '[object Boolean]') {
      json.type = 'boolean';
      json.sample = data[key] + "";
    } else if (Object.prototype.toString.call(data[key]) === '[object Object]') {
      json.type = 'object';
      if (JSON.stringify(data[key]) === "{}") {
        return;
      }
      json.children = [];
      let j = -1;
      for (let obj in data[key]) {
        let child = {};
        j++
        child.key = json.key + "-" + j;
        child.node = j;
        child.name = obj;
        child.level = 2;
        if (Object.prototype.toString.call(data[key][obj]) === '[object String]') {
          child.type = 'string';
          child.sample = data[key][obj].replace(/\“/g, "").replace(/\”/g, "");
        } else if (Object.prototype.toString.call(data[key][obj]) === '[object Number]') {
          child.type = 'int';
          child.sample = data[key][obj] + "";
        } else if (Object.prototype.toString.call(data[key][obj]) === '[object Boolean]') {
          child.type = 'boolean';
          child.sample = data[key][obj] + "";
        } else if (Object.prototype.toString.call(data[key][obj]) === '[object Object]') {
          child.type = 'object';
          if (JSON.stringify(data[key][obj]) === "{}") {
            return;
          }
          child.children = [];
          let k = -1;
          for (let con in data[key][obj]) {
            let grandson = {};
            k++;
            grandson.key = child.key + "-" + k;
            grandson.node = k;
            grandson.name = con;
            grandson.level = 3;
            if (Object.prototype.toString.call(data[key][obj][con]) === '[object String]') {
              grandson.type = 'string';
              grandson.sample = data[key][obj][con].replace(/\“/g, "").replace(/\”/g, "");
            } else if (Object.prototype.toString.call(data[key][obj][con]) === '[object Number]') {
              grandson.type = 'int';
              grandson.sample = data[key][obj][con] + "";
            } else if (Object.prototype.toString.call(data[key][obj][con]) === '[object Boolean]') {
              grandson.type = 'boolean';
              grandson.sample = data[key][obj][con] + "";
            } else if (Object.prototype.toString.call(data[key][obj][con]) === '[object Object]') {
              grandson.type = 'object';
              grandson.sample = JSON.stringify(data[key][obj][con]);
            } else if (Object.prototype.toString.call(data[key][obj][con]) === '[object Array]') {
              grandson.type = 'Array';
              grandson.sample = JSON.stringify(data[key][obj][con]);
            }
            child.children.push(grandson)
          }
        }
        json.children.push(child)
      }
    } else if (Object.prototype.toString.call(data[key]) === '[object Array]') {
      json.type = 'List';
      /*
      * 这里判断数组里面的类型是什么，普通类型，对象类型，数组类，这里对于数组里面的值交由后台校验
      * */
      if (Object.prototype.toString.call(data[key][0]) !== '[object Array]' && Object.prototype.toString.call(data[key][0]) !== '[object Object]') {
        json.sample = data[key] + "";
      } else if (Object.prototype.toString.call(data[key][0]) === '[object Object]') {
        json.children = [];
        let k = -1;
        data[key].forEach(item => {
          let child = {};
          k++;
          child.key = json.key + "-" + k;
          child.node=k;
          child.name="默认";
          child.level=2;
          if (Object.prototype.toString.call(item) === '[object String]') {
            child.type = 'string';
            child.sample = item.replace(/\“/g, "").replace(/\”/g, "");
          } else if (Object.prototype.toString.call(item) === '[object Number]') {
            child.type = 'int';
            child.sample = item + "";
          } else if (Object.prototype.toString.call(item) === '[object Boolean]') {
            child.type = 'boolean';
            child.sample = item + "";
          } else if (Object.prototype.toString.call(item) === '[object Object]'){
            child.type = 'object';
            child.children = [];
            let j = -1;
            for (let con in item) {
              let grandson = {};
              j++;
              grandson.key = child.key + "-" + j;
              grandson.node = j;
              grandson.name = con;
              grandson.level = 3;
              if (Object.prototype.toString.call(item[con]) === '[object String]') {
                grandson.type = 'string';
                grandson.sample = item[con].replace(/\“/g, "").replace(/\”/g, "");
              } else if (Object.prototype.toString.call(item[con]) === '[object Number]') {
                grandson.type = 'int';
                grandson.sample = item[con] + "";
              } else if (Object.prototype.toString.call(item[con]) === '[object Boolean]') {
                grandson.type = 'boolean';
                grandson.sample = item[con] + "";
              } else if (Object.prototype.toString.call(item[con]) === '[object Object]') {
                grandson.type = 'object';
                grandson.sample = JSON.stringify(item[con]);
              } else if (Object.prototype.toString.call(item[con]) === '[object Array]') {
                grandson.type = 'Array';
                grandson.sample = JSON.stringify(item[con]);
              }
              child.children.push(grandson)
            }
          } else if (Object.prototype.toString.call(item) === '[object Array]'){
            child.type = 'Array';
            child.children = [];
            let j = -1;
            item.forEach(it => {
              let grandson = {};
              j++;
              grandson.key = child.key + "-" + j;
              grandson.node = j;
              grandson.name = "默认";
              grandson.level = 3;
              if (Object.prototype.toString.call(it) === '[object String]') {
                grandson.type = 'string';
                grandson.sample = it.replace(/\“/g, "").replace(/\”/g, "");
              } else if (Object.prototype.toString.call(it) === '[object Number]') {
                grandson.type = 'int';
                grandson.sample = it + "";
              } else if (Object.prototype.toString.call(it) === '[object Boolean]') {
                grandson.type = 'boolean';
                grandson.sample = it + "";
              } else if (Object.prototype.toString.call(it) === '[object Object]') {
                grandson.type = 'object';
                grandson.sample = JSON.stringify(it);
              } else if (Object.prototype.toString.call(it) === '[object Array]') {
                grandson.type = 'Array';
                grandson.sample = JSON.stringify(it);
              }
              child.children.push(grandson)
            })
          }
          json.children.push(child)
        })
      } else if (Object.prototype.toString.call(data[key][0]) === '[object Array]') {
        json.children = [];
        let j = -1;
        data[key].forEach(item => {
          let child = {};
          j++
          child.key = json.key + "-" + j;
          child.node = j;
          child.name = "默认";
          child.level = 2;
          if (Object.prototype.toString.call(item[0]) === '[object String]') {
            child.type = 'string';
            child.sample = item.replace(/\“/g, "").replace(/\”/g, "");
          } else if (Object.prototype.toString.call(item[0]) === '[object Number]') {
            child.type = 'int';
            child.sample = item + "";
          } else if (Object.prototype.toString.call(item[0]) === '[object Boolean]') {
            child.type = 'boolean';
            child.sample = item + "";
          } else if (Object.prototype.toString.call(item[0]) === '[object Object]') {
            child.type = 'object';
            child.children = [];
            let k = -1;
            for (let obj in item) {
              let grandson = {};
              k++;
              grandson.key = child.key + "-" + k;
              grandson.node = k;
              grandson.name = "默认";
              grandson.level = 3;
              if (Object.prototype.toString.call(item[obj]) === '[object String]') {
                grandson.type = 'string';
                grandson.sample = item[obj].replace(/\“/g, "").replace(/\”/g, "");
              } else if (Object.prototype.toString.call(item[obj]) === '[object Number]') {
                grandson.type = 'int';
                grandson.sample = item[obj] + "";
              } else if (Object.prototype.toString.call(item[obj]) === '[object Boolean]') {
                grandson.type = 'boolean';
                grandson.sample = item[obj] + "";
              } else if (Object.prototype.toString.call(item[obj]) === '[object Object]') {
                grandson.type = 'object';
                grandson.sample = JSON.stringify(item[obj]);
              } else if (Object.prototype.toString.call(item[obj]) === '[object Array]') {
                grandson.type = 'Array';
                grandson.sample = JSON.stringify(item[obj]);
              }
              child.children.push(grandson)
            }
          } else if (Object.prototype.toString.call(item[0]) === '[object Array]') {
            child.type = 'Array';
            child.children = [];
            let k = -1;
            item.forEach(it => {
              let grandson = {};
              k++;
              grandson.key = child.key + "-" + k;
              grandson.node = k;
              grandson.name = "默认";
              grandson.level = 3;
              if (Object.prototype.toString.call(it) === '[object String]') {
                grandson.type = 'string';
                grandson.sample = it.replace(/\“/g, "").replace(/\”/g, "");
              } else if (Object.prototype.toString.call(it) === '[object Number]') {
                grandson.type = 'int';
                grandson.sample = it + "";
              } else if (Object.prototype.toString.call(it) === '[object Boolean]') {
                grandson.type = 'boolean';
                grandson.sample = it + "";
              } else if (Object.prototype.toString.call(it) === '[object Object]') {
                grandson.type = 'object';
                grandson.sample = JSON.stringify(it);
              } else if (Object.prototype.toString.call(it) === '[object Array]') {
                grandson.type = 'Array';
                grandson.sample = JSON.stringify(it);
              }
              child.children.push(grandson)
            })
          }
          json.children.push(child)
        })
      }
    }
    arr.push(json);
  }
  return arr
}
export function filterIpWhiteList(data){//IP白名单
  let str = [];
  data.forEach(item => {
    if (item.name) {
      str.push(item.name)
    }
  })
  return str.length?str:null
}
export function validateRoute(rule, value, callback) {//创建API，路由规则校验
  let reg = /^[/A-Za-z0-9-~.;:@&%=!*'+()_,\?]+$/
  for (let i = 0; i < value.length; i++) {
    if (!value[i].sample) {
      callback(new Error('请填写完整的路由规则'));
      return;
    } else if (!value[i].type.length) {
      callback(new Error('请填写完整的路由规则'));
      return;
    } else if (!reg.test(value[i].sample)) {
      callback(new Error('请正确填写的路由规则'));
      return;
    }
  }
  callback();
}


function xmlStr2XmlObj(xmlStr) {//xml字符串转换json数据
  let xmlObj = {};
  if (document.all) {
    let xmlDom = new ActiveXObject("Microsoft.XMLDOM");
    xmlDom.loadXML(xmlStr);
    xmlObj = xmlDom;
  } else {
    xmlObj = new DOMParser().parseFromString(xmlStr, "text/xml");
  }
  return xmlObj;
}
function xml2json(xml) {//xml转换json数据
  try {
    let obj = {};
    if (xml.children.length > 0) {
      for (let i = 0; i < xml.children.length; i++) {
        let item = xml.children.item(i);
        let nodeName = item.nodeName;
        if (typeof (obj[nodeName]) == "undefined") {
          obj[nodeName] = xml2json(item);
        } else {
          if (typeof (obj[nodeName].push) == "undefined") {
            let old = obj[nodeName];
            obj[nodeName] = [];
            obj[nodeName].push(old);
          }
          obj[nodeName].push(xml2json(item));
        }
      }
    } else {
      obj = xml.textContent;
    }
    return obj;
  } catch (e) {}
}

export function xmlObj2json(xml) {// xml字符串转换xml对象数据
  let xmlObj = xmlStr2XmlObj(xml);
  let jsonObj = {};
  if (xmlObj.childNodes.length > 0) {
    jsonObj = xml2json(xmlObj);
  }
  return jsonObj;
}
