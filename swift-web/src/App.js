import {Admin, Resource} from 'react-admin'
import React, {Component} from 'react'
import jsonServerProvider from 'ra-data-json-server'
import authProvider from './authProvider'
import restProvider from './rest/restProvider'
import Login from './layout/Login'
import Dashboard from "./dashboard"
import NotFound from './components/NotFound'
import system from './components/system'
import segment from './components/segment'
import table from './components/table'
import SegmentLocaionList from './components/segment/SegmentLocationList'


const dataProvider = jsonServerProvider('http://localhost:8888/swift/api');

const restDataProvider = restProvider(dataProvider);

class App extends Component {
    render() {
        return (<Admin dashboard={Dashboard} authProvider={authProvider} dataProvider={restDataProvider}
                       loginPage={Login}
                       catchAll={NotFound}>
                <Resource name="table" {...table} />
                <Resource name="segment" options={{label: 'segment'}} {...segment} />
                <Resource name="location/segment" options={{label: 'segmentLocation'}} list={SegmentLocaionList}/>
                <Resource name="service" options={{label: 'system'}} {...system}/>
            </Admin>
        )
    }
}

export default App;