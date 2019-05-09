import {AUTH_CHECK, AUTH_ERROR, AUTH_LOGIN, AUTH_LOGOUT} from 'react-admin';
import axios from 'axios';

export default (type, params) => {
    if (type === AUTH_LOGIN) {
        const {username, password} = params;
        return axios({
            method: 'post',
            url: 'http://localhost:8888/swift/api/user/login',
            data: {
                account: username,
                password: password
            }
        }).then(successResponse => {
            var status =successResponse.data.statusCode;
            if(status === 0 ) {
                var account = successResponse.data.data;
                localStorage.setItem('username', account);
                return Promise.resolve();
            } else {
                alert("username or password wrong!")
            }
        }).catch(errorResponse =>{
            alert("login failed")
        });
    }
    if (type === AUTH_LOGOUT) {
        localStorage.removeItem('username');
        return Promise.resolve();
    }
    if (type === AUTH_ERROR) {
        const {status} = params;
        if (status === 401 || status === 403) {
            localStorage.removeItem('username');
            return Promise.reject();
        }
        return Promise.resolve();
    }
    if (type === AUTH_CHECK) {
        return localStorage.getItem('username')
            ? Promise.resolve()
            : Promise.reject();
    }
    return Promise.reject('Unknown method');
};