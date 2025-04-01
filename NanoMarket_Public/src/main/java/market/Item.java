package market;

import com.google.gson.Gson;
import java.util.List;
import java.util.ArrayList;

public class Item {
        private String description;
        private String country;
        private String email;
        private String title;
        private String cancel;
        private String tag1;
        private String tag2;
        private String tag3;
        private boolean active;
        private long timestamp;
        private String image;
        private String id;
        private String address;
        private String cost;
        private String quantity;
        private boolean paid;
        private String seller;
        private String cancelID;
        private String totalLocked;
        private String date;



	private ArrayList<Buyer> buyerList = new ArrayList<>();

		//nested class
		public static class Buyer {
			private String buyerAddress;
        	private String quantity;
        	private String email;
			private String approved;
			private String invoiceID;
            private String lockedFunds;
            private String lockTimestamp;
            private String lockDate;
            private String publicOrderID;
            
            public String getPublicOrderID() {
            			return publicOrderID;
        	}
            public void setPublicOrderID(String publicOrderID) {
                        this.publicOrderID = publicOrderID;
        	}

            public String getLockedFunds() {
            			return lockedFunds;
        	}
            public void setLockedFunds(String lockedFunds) {
               this.lockedFunds = lockedFunds;

        	}

			public String getBuyerAddress() {
            			return buyerAddress;
        		}
            public String getLockTimestamp() {
            			return lockTimestamp;
        	}
            public String getLockDate() {
            			return lockDate;
        	}

			public String getQuantity() {
            			return quantity;
        		}
			public String getEmail() {
            			return email;
        		}
			public String isApproved() {
            			return approved;
        		}

			public String getInvoiceID() {
            			return invoiceID;
        	}

			public void setInvoiceID(String invoiceID) {
                        	this.invoiceID = invoiceID;
                	}
			public void setQuantity(String quantity) {
                        	this.quantity = quantity;
                	}
			public void setEmail(String email) {
                        	this.email = email;
                	}
			public void setBuyerAddress(String buyerAddress) {
            			this.buyerAddress = buyerAddress;
        		}
			public void setApproved(String approved) {
                        	this.approved = approved;
                	}
            public void setLockTimestamp(String lockTimestamp) {
                        	this.lockTimestamp = lockTimestamp;
                	}
            public void setLockDate(String lockDate) {
                        	this.lockDate = lockDate;
                	}


		}
        // Getters and setters for all fields
        //
        public String getCancelID() {
                return cancelID;
        }
        
        public void setCancelID() {
                this.cancelID = cancelID;
        }
        public String getLocked() {
            	return totalLocked;
        }
        public void setLocked(String totalLocked) {
            this.totalLocked = totalLocked;
            }



		public ArrayList<Buyer> getBuyerList() {
        		return buyerList;
    		}

    		public void setBuyerList(ArrayList<Buyer> buyerList) {
        		this.buyerList = buyerList;
   		}	

                public String getDescription() {
                        return description;
                }

                public void setDescription(String description) {
                        this.description = description;
                }

                public String getEmail() {
                        return email;
                }

                public void setEmail(String email) {
                        this.email = email;
                }

                public String getTitle() {
                        return title;
                }
                public String getCountry() {
                        return country;
                }

                public void setTitle(String title) {
                        this.title = title;
                }

                public String getTag1() {
                        return tag1;
                }
                public String getDate() {
                    return date;
                }
                public void setDate(String date) {
                    this.date = date;
                }
                public void setTag1(String tag1) {
                        this.tag1 = tag1;
                }

                public String getTag2() {
                        return tag2;
                }

                public void setTag2(String tag2) {
                        this.tag2 = tag2;
                }

                public String getTag3() {
                        return tag3;
                }

                public void setTag3(String tag3) {
                        this.tag3 = tag3;
                }

                public boolean isActive() {
                        return active;
                }

                public void setActive(boolean active) {
                        this.active = active;
                }

                public long getTimestamp() {
                        return timestamp;
                }

                public void setTimestamp(long timestamp) {
                        this.timestamp = timestamp;
                }

                public String getImage() {
                        return image;
                }

                public void setImage(String image) {
                        this.image = image;
                }

                public String getId() {
                        return id;
                }

                public void setId(String id) {
                        this.id = id;
                }

                public String getAddress() {
                        return address;
                }

                public void setAddress(String address) {
                        this.address = address;
                }

                public String getCost() {
                        return cost;
                }

                public void setCost(String cost) {
                        this.cost = cost;
                }

                public String getQuantity() {
                        return quantity;
                }

                public void setQuantity(String quantity) {
                        this.quantity = quantity;
                }

                public boolean isPaid() {
                        return paid;
                }

                public void setPaid(boolean paid) {
                        this.paid = paid;
                }

                public String getSeller() {
                        return seller;
                }

                public void setSeller(String seller) {
                        this.seller = seller;
                }
                public void setCancel(String cancel) {
                        this.cancel = cancel;
                }
                public String getCancel() {
                    return cancel;
                }
                
		public String toJson() {
        		Gson gson = new Gson();
        		return gson.toJson(this);
    		}

}
