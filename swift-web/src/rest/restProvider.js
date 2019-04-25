import {stringify} from 'query-string';
import {
    CREATE,
    DELETE,
    DELETE_MANY,
    GET_LIST,
    GET_MANY,
    GET_MANY_REFERENCE,
    GET_ONE,
    UPDATE,
    UPDATE_MANY
} from 'react-admin';
import axios from 'axios';
// const apiUrl = 'http://jsonplaceholder.typicode.com';
const apiUrl = 'http://localhost:8888/swift/api';


const restProvider = requestHandler => (type, resource, params) => {
    let url = '';
    const options = {
        headers: new Headers({
            Accept: 'application/json',
        }),
    }
    if (!options.headers) {
        options.headers = new Headers({Accept: 'application/json'});
    }
    options.headers.set('X-Custom-Header', 'foobar');

    switch (type) {
        case GET_LIST: {
            const {page, perPage} = params.pagination;
            const {field, order} = params.sort;
            const query = {
                sort: JSON.stringify([field, order]),
                range: JSON.stringify([
                    (page - 1) * perPage,
                    page * perPage - 1,
                ]),
                filter: JSON.stringify(params.filter),
            };
            url = `${apiUrl}/${resource}?${stringify(query)}`;
            options.method = 'GET';
            break;
        }
        case GET_ONE:
            url = `${apiUrl}/${resource}/${params.id}`;
            options.method = 'GET';
            break;
        case CREATE:
            url = `${apiUrl}/${resource}`;
            options.method = 'POST';
            options.body = JSON.stringify(params.data);
            break;
        case UPDATE:
            url = `${apiUrl}/${resource}/${params.id}`;
            options.method = 'PUT';
            options.body = JSON.stringify(params.data);
            break;
        case UPDATE_MANY: {
            const query = {
                filter: JSON.stringify({id: params.ids}),
            };
            url = `${apiUrl}/${resource}?${stringify(query)}`;
            options.method = 'PATCH';
            options.body = JSON.stringify(params.data);
            break;
        }
        case DELETE:
            url = `${apiUrl}/${resource}/${params.id}`;
            options.method = 'DELETE';
            break;
        case DELETE_MANY:
            const query = {
                filter: JSON.stringify({id: params.ids}),
            };
            url = `${apiUrl}/${resource}?${stringify(query)}`;
            options.method = 'DELETE';
            break;
        case GET_MANY: {
            const query = {
                filter: JSON.stringify({id: params.ids}),
            };
            url = `${apiUrl}/${resource}?${stringify(query)}`;
            break;
        }
        case GET_MANY_REFERENCE: {
            const {page, perPage} = params.pagination;
            const {field, order} = params.sort;
            const query = {
                sort: JSON.stringify([field, order]),
                range: JSON.stringify([
                    (page - 1) * perPage,
                    page * perPage - 1,
                ]),
                filter: JSON.stringify({
                    ...params.filter,
                    [params.target]: params.id,
                }),
            };
            url = `${apiUrl}/${resource}?${stringify(query)}`;
            break;
        }
        default:
            throw new Error(`Unsupported Data Provider request type ${type}`);
    }

    return axios({
        method: options.method,
        url: url,
        data: {
            requestBody: options.body
        }
    }).then(successResponse => {
        console.log(successResponse);
        var result = successResponse.data.data;
        result.map((ele, index) => (
            ele["id"] = ele["id"] ? ele["id"] : index
        ))
        var totalSize = successResponse.data.dataHeaders.totalSize;
        switch (type) {
            case GET_LIST:
            case GET_MANY_REFERENCE:
                return {
                    data: result,
                    total: totalSize
                };
            case CREATE:
                return {data: {...params.data, id: result.id}};
            default:
                return {data: result};
        }
    }).catch(failResponse => {
        console.log(failResponse);
    });
};

export default restProvider;