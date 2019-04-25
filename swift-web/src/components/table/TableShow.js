import React from 'react';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';

const TableShow = ({id, record, resource}) => (
    <Card style={{width: 1000, margin: 'auto'}}>
        <CardContent>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell align="right">name</TableCell>
                        <TableCell align="right">type</TableCell>
                        <TableCell align="right">remark</TableCell>
                        <TableCell align="right">precision</TableCell>
                        <TableCell align="right">scale</TableCell>
                        <TableCell align="right">columnId</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {record.fields.map(field => (
                        <TableRow key={field.name}>
                            <TableCell align="right">{field.name}</TableCell>
                            <TableCell align="right">{field.type}</TableCell>
                            <TableCell align="right">{field.remark}</TableCell>
                            <TableCell align="right">{field.precision}</TableCell>
                            <TableCell align="right">{field.scale}</TableCell>
                            <TableCell align="right">{field.columnId}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </CardContent>
    </Card>
)

export default TableShow;