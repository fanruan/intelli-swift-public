import React from 'react';
import {
    Create,
    Datagrid,
    DisabledInput,
    Edit,
    Filter,
    List,
    LongTextInput,
    ReferenceField,
    ReferenceInput,
    SelectInput,
    SimpleForm,
    TextField,
    TextInput
} from 'react-admin';

const PostFilter = (props) => (
    <Filter {...props}>
        <TextInput label="Search" source="q" alwaysOn/>
        <ReferenceInput label="User" source="userId" reference="users" allowEmpty>
            <SelectInput optionText="name"/>
        </ReferenceInput>
    </Filter>
);

const PostList = props => (
    <List {...props} filters={<PostFilter/>}>
        <Datagrid rowClick="edit">
            <ReferenceField source="userId" reference="users">
                <TextField source="id"/>
            </ReferenceField>
            <ReferenceField source="userId" reference="users">
                <TextField source="name"/>
            </ReferenceField>
            <TextField source="id"/>
            <TextField source="title"/>
            <TextField source="body"/>

        </Datagrid>
    </List>
);
const PostCreate = props => (
    <Create {...props}>
        <SimpleForm>
            <ReferenceInput source="userId" reference="users">
                <SelectInput optionText="name"/>
            </ReferenceInput>
            <TextInput source="title"/>
            <LongTextInput source="body"/>
        </SimpleForm>
    </Create>
);
const PostEdit = props => (
    <Edit {...props}>
        <SimpleForm>
            <DisabledInput source="id"/>
            <ReferenceInput source="userId" reference="users">
                <SelectInput optionText="name"/>
            </ReferenceInput>
            <TextInput source="title"/>
            < LongTextInput source="body"/>
        </SimpleForm>
    </Edit>
);

export default {PostList, PostEdit, PostCreate}
