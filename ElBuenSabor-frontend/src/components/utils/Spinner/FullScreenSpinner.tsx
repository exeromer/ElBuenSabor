import React from 'react';
import { Spinner } from 'react-bootstrap';
import './FullScreenSpinner.sass';

const FullScreenSpinner: React.FC = () => {
    return (
        <div className="spinner-overlay">
            <Spinner animation="border" variant="primary" role="status">
                <span className="visually-hidden">Cargando...</span>
            </Spinner>
        </div>
    );
};

export default FullScreenSpinner;