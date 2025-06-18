package com.powerRanger.ElBuenSabor.entities.enums;

public enum TipoPromocion {
    PORCENTAJE, // Descuento por un porcentaje del precio original
    CANTIDAD,   // Descuento por precio fijo (ej. 2x1, combos, precio especial por unidad)
    COMBO       // Promociones que agrupan varios artículos a un precio especial
}