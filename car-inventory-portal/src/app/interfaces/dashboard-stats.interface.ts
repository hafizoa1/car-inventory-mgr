export interface DashboardStats {
    inventory: {
      total: number;
      active: number;
      sold: number;
    };
    leads: {
      new: number;
      open: number;
      closed: number;
    };
    ads: {
      active: number;
      views: number;
      ctr: number;  
      conversion: number;
    };
 }
 