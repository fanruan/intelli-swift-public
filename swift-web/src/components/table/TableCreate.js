import React from "react";
import {
    ArrayInput,
    Create,
    NumberInput,
    required,
    SelectInput,
    SimpleForm,
    SimpleFormIterator,
    TextInput
} from 'react-admin';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';


const TableCreate = props => (

        <Create {...props}>
            <SimpleForm>
                <Card>
                    <CardContent>
                        <TextInput source="tableName" validate={required()}/>
                        <ArrayInput source="fields">
                            <SimpleFormIterator validate={required()}>
                                <TextInput source="name" label="fieldName" validate={required()}/>
                                <SelectInput source="type" label="fieldType" choices={[
                                    {id: '4', name: 'integer'},
                                    {id: '-5', name: 'long'},
                                    {id: '8', name: 'double'},
                                    {id: '91', name: 'date'},
                                    {id: '12', name: 'string'},
                                ]} validate={required()}/>
                                <NumberInput source="precision" validate={required()}/>
                                <NumberInput source="scale" validate={required()}/>
                            </SimpleFormIterator>
                        </ArrayInput>
                    </CardContent>
                </Card>
            </SimpleForm>
        </Create>
    )
;

export default TableCreate;