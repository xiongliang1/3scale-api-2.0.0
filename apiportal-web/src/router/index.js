import Vue from 'vue'
import Router from 'vue-router'
import Cookie from "js-cookie";
const main =()=>import('@/components/main');
const list =()=>import('@/components/list');
const portDetails =()=>import('@/components/portDetails');
const developerCenter =()=>import('@/components/developerCenter');
const mySubscription =()=>import('@/components/childComponents/mySubscription');
const myApply =()=>import('@/components/childComponents/myApply');
const myCollection =()=>import('@/components/childComponents/myCollection');
const flowChart =()=>import('@/components/childComponents/flowChart');
const logs =()=>import('@/components/childComponents/logs');
const dashboard =()=>import('@/components/childComponents/dashboard');
const apiDetails =()=>import('@/components/apiDetails');
const login =()=>import('@/components/login');
const principal =()=>import('@/components/principal');
const monitoring =()=>import('@/components/monitoring');
if (Cookie.get('userName')){
  if (typeof window._paq !== 'undefined' ) {
    _paq.push(['setUserId', Cookie.get('userName')]);
  }
}
Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/login',
      name: 'login',
      component: login
    },
    {
      path: '/principal',
      name: 'principal',
      component: principal,
      children:[
        {
          path: '/principal/main',
          name: 'main',
          component: main
        },
        {
          path: '/principal/list',
          name: 'list',
          component: list
        },
        {
          path: '/principal/portDetails',
          name: 'portDetails',
          component: portDetails
        },
        {
          path: '/principal/developerCenter',
          name: 'developerCenter',
          component: developerCenter,
          redirect: '/mySubscription',
          children:[
            {
              path: '/principal/developerCenter/mySubscription',
              name: 'mySubscription',
              component: mySubscription,
            },
            {
              path: '/principal/developerCenter/myApply',
              name: 'myApply',
              component: myApply
            },
            {
              path: '/principal/developerCenter/flowChart',
              name: 'flowChart',
              component: flowChart
            },
            {
              path: '/principal/developerCenter/myCollection',
              name: 'myCollection',
              component: myCollection
            },
            {
              path: '/principal/developerCenter/logs',
              name: 'logs',
              component: logs
            },
            {
              path: '/principal/developerCenter/dashboard',
              name: 'dashboard',
              component: dashboard
            }
          ]
        },
        {
          path: '/principal/apiDetails',
          name: 'apiDetails',
          component: apiDetails
        },
        {
          path: '/principal/monitoring',
          name: 'monitoring',
          component: monitoring
        }
      ]
    }
  ]
})
