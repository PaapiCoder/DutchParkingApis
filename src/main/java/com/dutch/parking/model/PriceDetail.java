package com.dutch.parking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "PriceDetail")
public class PriceDetail {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "street_name")
	private String streetName;

	@Column(name = "price")
	private BigDecimal price;
}
