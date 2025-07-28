import React from 'react';
import { Pagination } from 'react-bootstrap';
import './PaginationControls.sass';

interface PaginationControlsProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

const PaginationControls: React.FC<PaginationControlsProps> = ({ currentPage, totalPages, onPageChange }) => {
  if (totalPages <= 1) {
    return null; 
  }

  const handlePageClick = (page: number) => {
    if (page >= 1 && page <= totalPages) {
      onPageChange(page);
    }
  };

  const items = [];
  for (let number = 1; number <= totalPages; number++) {
    items.push(
      <Pagination.Item key={number} active={number === currentPage} onClick={() => handlePageClick(number)}>
        {number}
      </Pagination.Item>,
    );
  }

  return (
    <Pagination className="custom-pagination justify-content-center mt-4">
      <Pagination.Prev onClick={() => handlePageClick(currentPage - 1)} disabled={currentPage === 1} />
      {items}
      <Pagination.Next onClick={() => handlePageClick(currentPage + 1)} disabled={currentPage === totalPages} />
    </Pagination>
  );
};

export default PaginationControls;