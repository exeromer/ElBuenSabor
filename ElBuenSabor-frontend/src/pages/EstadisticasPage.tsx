import React, { useState } from 'react';
import { Container, Tabs, Tab, Alert } from 'react-bootstrap';
import Titulo from '../components/utils/Titulo/Titulo';
import ProductRankingTab from '../components/estadisticas/ProductRankingTab'; 
import ClientRankingTab from '../components/estadisticas/ClientRankingTab'; 
import MonetaryMovementTab from '../components/estadisticas/MonetaryMovementTab'; 
import { useSucursal } from '../context/SucursalContext'; // Para asegurar que una sucursal esté seleccionada

const EstadisticasPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<string>('productRanking');
  const { selectedSucursal } = useSucursal();

  // Comprobación básica si una sucursal está seleccionada, ya que algunos informes pueden depender de ello.
  if (!selectedSucursal) {
    return (
      <Container className="my-5">
        <Alert variant="info" className="text-center">
          Por favor, selecciona una sucursal para ver las estadísticas.
        </Alert>
      </Container>
    );
  }

  return (
    <Container className="my-4">
      <Titulo texto="Panel de Estadísticas e Informes" nivel="titulo" />

      <Tabs
        id="estadisticas-tabs"
        activeKey={activeTab}
        onSelect={(k) => setActiveTab(k!)}
        className="mb-3 justify-content-center"
        fill
      >
        {/* **INICIO DE CÓDIGO NUEVO** */}
        <Tab eventKey="productRanking" title="Ranking de Productos">
          <ProductRankingTab />
        </Tab>
        <Tab eventKey="clientRanking" title="Ranking de Clientes">
          <ClientRankingTab />
        </Tab>
        <Tab eventKey="monetaryMovements" title="Movimientos Monetarios">
          <MonetaryMovementTab />
        </Tab>
        {/* **FIN DE CÓDIGO NUEVO** */}
      </Tabs>
    </Container>
  );
};

export default EstadisticasPage;