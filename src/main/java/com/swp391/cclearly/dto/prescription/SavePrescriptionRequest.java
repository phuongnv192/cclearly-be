package com.swp391.cclearly.dto.prescription;

import lombok.Data;

@Data
public class SavePrescriptionRequest {
  private String orderItemId; // UUID of the order item

  // Right eye (OD)
  private Float sphOd;
  private Float cylOd;
  private Float axisOd;
  private Float addOd;

  // Left eye (OS)
  private Float sphOs;
  private Float cylOs;
  private Float axisOs;
  private Float addOs;

  // PD
  private Float pd;

  // Prescription image/notes
  private String imageUrl;
  private String salesNote;
}
