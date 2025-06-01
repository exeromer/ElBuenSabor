import React from 'react';
import type { ReactNode } from 'react';
import { Table, Form, Spinner, Alert, Button } from 'react-bootstrap';
import type { SortConfig } from '../../../hooks/useSearchableData'; // Ajusta la ruta
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSort, faSortUp, faSortDown, faPlus } from '@fortawesome/free-solid-svg-icons';

export interface ColumnDefinition<T> {
  key: string;
  header: string;
  renderCell: (item: T) => ReactNode;
  sortable?: boolean;
}

interface SearchableTableProps<T extends { id: any }> {
  items: T[]; // Recibe directamente los items a mostrar
  searchTerm: string;
  setSearchTerm: (term: string) => void;
  isLoading: boolean;
  error: string | null;
  reload: () => void;
  sortConfig: SortConfig<T> | null;
  requestSort: (key: keyof T | string) => void;
  columns: ColumnDefinition<T>[];
  searchPlaceholder?: string;
  renderRowActions?: (item: T, reloadData: () => void) => ReactNode;
  createButtonText?: string;
  onCreate?: () => void;
}

export function SearchableTable<T extends { id: any }>({
  items,
  searchTerm,
  setSearchTerm,
  isLoading,
  error,
  reload,
  sortConfig,
  requestSort,
  columns,
  searchPlaceholder = "Buscar...",
  renderRowActions,
  createButtonText,
  onCreate,
}: SearchableTableProps<T>) {
  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(event.target.value);
  };
    const getSortIcon = (columnKey: keyof T | string) => {
    if (!sortConfig || sortConfig.key !== columnKey) {
      return faSort; // Icono por defecto para columnas ordenables
    }
    return sortConfig.direction === 'ascending' ? faSortUp : faSortDown;
  };

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <Form.Control
          type="text"
          placeholder={searchPlaceholder}
          value={searchTerm}
          onChange={handleSearchChange}
          style={{ maxWidth: '400px' }}
        />
        {onCreate && createButtonText && (
          <Button variant="success" onClick={onCreate}>
            {<FontAwesomeIcon icon={faPlus} className="me-2" /> }
            {createButtonText}
          </Button>
        )}
      </div>

      {isLoading && <div className="text-center my-3"><Spinner animation="border" /> <p>Cargando...</p></div>}
      {error && <Alert variant="danger">Error: {error}</Alert>}

      {!isLoading && !error && items.length === 0 && (
        <Alert variant="info">
          {searchTerm ? `No se encontraron resultados para "${searchTerm}".` : "No hay datos para mostrar."}
        </Alert>
      )}

    {!isLoading && !error && items.length > 0 && (
        <>
          <Table striped bordered hover responsive className="text-center align-middle">
            <thead>
              <tr>
                {columns.map(col => (
                  <th 
                    key={col.key} 
                    onClick={() => col.sortable ? requestSort(col.key as keyof T) : undefined}
                    style={col.sortable ? { cursor: 'pointer' } : {}}
                  >
                    {col.header}
                    {col.sortable && (
                      <FontAwesomeIcon icon={getSortIcon(col.key as keyof T)} className="ms-2" />
                    )}
                  </th>
                ))}
                {renderRowActions && <th>Acciones</th>}
              </tr>
            </thead>
            <tbody>
              {items.map(item => (
                <tr key={item.id}>
                  {columns.map(col => (
                    <td key={`${item.id}-${col.key}`}>{col.renderCell(item)}</td>
                  ))}
                  {renderRowActions && <td>{renderRowActions(item, reload)}</td>}
                </tr>
              ))}
            </tbody>
          </Table>
          {/* Sección de Paginación eliminada */}
        </>
      )}
    </div>
  );
}